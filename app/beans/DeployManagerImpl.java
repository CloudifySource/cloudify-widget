/*
 * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package beans;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.inject.Inject;

import models.ServerNode;
import models.ServerNodeEvent;
import models.Widget;
import models.WidgetInstance;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import play.libs.Json;
import server.DeployManager;
import utils.StringUtils;
import utils.Utils;
import beans.api.ExecutorFactory;
import beans.cloudify.CloudifyRestClient;
import beans.config.Conf;
import beans.scripts.ScriptExecutor;

/**
 * This class deploys a recipe file vi cloudify non-interactive CLI.
 * Each deploy forks a CLI process and stream the output.
 *
 * @author Igor Goldenberg
 */
public class DeployManagerImpl implements DeployManager {

    private static Logger logger = LoggerFactory.getLogger(DeployManagerImpl.class);

    @Inject
    private Conf conf;

    @Inject
    private ExecutorFactory executorFactory;

    @Inject
    private CloudifyRestClient cloudifyRestClient;

    @Inject
    private ScriptExecutor scriptExecutor;


    @Override
    public WidgetInstance uninstall(ServerNode serverNode) {
        WidgetInstance widgetInstance = serverNode.getWidgetInstance();
        String installName = widgetInstance.getInstallName();
        // TODO : maybe we should verify it is installed using the rest client?
        File script = widgetInstance.getRecipeType() == Recipe.Type.APPLICATION ? conf.cloudify.uninstallApplicationScript : conf.cloudify.uninstallServiceScript;
        CommandLine cmdLine = new CommandLine(script);
        cmdLine.addArgument(serverNode.getPublicIP());
        cmdLine.addArgument(installName);
        logger.info("executing command [{}]", cmdLine);
        execute(cmdLine, serverNode);
        return widgetInstance;
    }


    /**
     * Decide if this is an application or a service
     * <p/>
     * if Application
     * Use a rest API call to list all the
     * http://IP:8100/service/applications
     * <p/>
     * if service
     * Use a rest API to list all services on default
     * http://IP:8100/service/applications/default/services
     * <p/>
     * view the results and decide if it is installed
     *
     * @param server
     * @param widget
     * @return
     */
    private boolean alreadyInstalled(ServerNode server, Widget widget, Recipe.Type recipeType) {

        logger.info("checking if [{}] named [{}] is installed on [{}]", new Object[]{recipeType, widget.toInstallName(), server.getPublicIP()});
        try {
            switch (recipeType) {

                case APPLICATION: {
                    return cloudifyRestClient.listApplications(server.getPublicIP()).response.containsKey(widget.toInstallName());
                }
                case SERVICE: {
                    // check if application "default" is installed at all, and if so - check for services on it.
                    if (cloudifyRestClient.listApplications(server.getPublicIP()).response.containsKey("default")) {
                        return cloudifyRestClient.listServices(server.getPublicIP(), "default").response.contains(widget.toInstallName());
                    } else {
                        logger.info("figured that application 'default' is not installed, and hence the service is not installed");
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("unable to decide if [{}] named [{}] is already installed on [{}]", new Object[]{recipeType, widget.toInstallName(), server});
        }

        return false; // todo
    }

    @Override
    public String getServicePublicIp(WidgetInstance widgetInstance) {
        try {
            logger.info("getting public IP for [{}]", widgetInstance);
            ServerNode server = widgetInstance.getServerNode();
            Widget widget = widgetInstance.getWidget();
            Recipe.Type recipeType = widgetInstance.getRecipeType();
            if (recipeType == Recipe.Type.SERVICE) {
                return cloudifyRestClient.getPublicIp(server.getPublicIP(), "default", widget.toInstallName()).cloudPublicIp;
            } else if (!StringUtils.isEmptyOrSpaces(widget.getConsoleUrlService())) { // this is an application and we need to get ip for specific service
                return cloudifyRestClient.getPublicIp(server.getPublicIP(), widget.toInstallName(), widget.getConsoleUrlService()).cloudPublicIp;
            }
        } catch (Exception e) {
            logger.error("unable to resolve public ip for widget instance [{}]", widgetInstance, e);
        }
        return null;
    }


    // todo - replace widget with widgetInstance - since we have server 1to1 widgetInstance, we can simply transfer server here.
    // todo - Widget should only be a template. We are installing a single instance.
    public WidgetInstance fork(ServerNode server, Widget widget) {
        File unzippedDir = null;
        File recipeDir = null;
        Recipe recipe = null;
        Recipe.Type recipeType = null;

        try {
            String recipeURL = widget.getRecipeURL();
            if (StringUtils.isEmpty(recipeURL)) {
                logger.info("recipe url is null. nothing to execute");
                server.createEvent("recipe already installed", ServerNodeEvent.Type.DONE).save();
                return widget.addWidgetInstance(server, null);

            }
            logger.info(" Widget [{}] has recipe url [{}]", widget.getRecipeName(), recipeURL);
            unzippedDir = Utils.downloadAndUnzip(recipeURL, widget.getApiKey());
            recipeDir = unzippedDir;
            if (widget.getRecipeRootPath() != null) {
                recipeDir = new File(unzippedDir, widget.getRecipeRootPath());
            }
            logger.info("Deploying an instance for recipe at : [{}] ", recipeDir);

            recipe = new Recipe(recipeDir);
            recipeType = recipe.getRecipeType();
        } catch (RuntimeException e) {
            server.createEvent(e.getMessage(), ServerNodeEvent.Type.ERROR).save();
            throw e;
        }

        if (alreadyInstalled(server, widget, recipeType)) {
            logger.info("[{}] [{}] is already installed", recipeType, widget.toInstallName());
            WidgetInstance widgetInstance = widget.addWidgetInstance(server, recipeDir);
            String publicIp = getServicePublicIp(widgetInstance);
            if (!StringUtils.isEmpty(publicIp)) {
                logger.info("found service ip at [{}]", publicIp);
                widgetInstance.setServicePublicIp(publicIp);
                widgetInstance.save();
            }
            server.createEvent(null, ServerNodeEvent.Type.DONE).save();

            return widgetInstance;
        } else {
            if (!StringUtils.isEmptyOrSpaces(server.getRecipeProperties())) {
                logger.info("user passed properties for the recipe. writing them to a file");
                File propertiesFile = recipe.getPropertiesFile();
                Collection<String> newLines = new LinkedList<String>();
                JsonNode recipePropertiesJson = Json.parse(server.getRecipeProperties());

                int i = 0;
                while (recipePropertiesJson.has(i)) {
                    JsonNode iNode = recipePropertiesJson.get(i);
                    newLines.add(iNode.get("key").getTextValue() + "=" + StringUtils.wrapWithQuotes(iNode.get("value").getTextValue()));
                    i++;
                }


                try {
                    FileUtils.writeLines(propertiesFile, newLines, true);
                } catch (Exception e) {
                    throw new RuntimeException("unable to write lines to properties file", e);
                }

            }
            logger.info("Deploying: [ServerIP={}] [recipe={}] [type={}]", new Object[]{server.getPublicIP(), recipeDir, recipeType.name()});
            String recipePath = FilenameUtils.separatorsToSystem(recipeDir.getPath());

            CommandLine cmdLine = new CommandLine(conf.cloudify.deployScript);
            cmdLine.addArgument(server.getPublicIP());
            cmdLine.addArgument(recipePath.replaceAll("\\\\", "/")); // support for windows.
            cmdLine.addArgument(recipeType.commandParam);
            cmdLine.addArgument(widget.toInstallName());


            Logger cliOutputLogger = LoggerFactory.getLogger("cliOutput");
            MDC.put("servernodeid", server.getId().toString());
            cliOutputLogger.info("deploying " + widget.toDebugString());
            cliOutputLogger.info("on machine " + server.toDebugString());
            cliOutputLogger.info("running command " + cmdLine.toString());

            execute(cmdLine, server);
            return widget.addWidgetInstance(server, recipeDir);
        }
    }

    private void execute(CommandLine cmdLine, ServerNode server) {
        scriptExecutor.runInstallationManagementScript(cmdLine, server);
    }


    public void setConf(Conf conf) {
        this.conf = conf;
    }

    public void setCloudifyRestClient(CloudifyRestClient cloudifyRestClient) {
        this.cloudifyRestClient = cloudifyRestClient;
    }
}