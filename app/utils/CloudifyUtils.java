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
package utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeServiceContext;

import server.ApplicationContext;
import beans.config.ServerConfig.CloudBootstrapConfiguration;


/**
 * This class provides different static cloudify utilities methods.
 * @author adaml
 *
 */
public class CloudifyUtils {
	
	/**
	 * returns the private key used for starting the remote machines.
	 * 
	 * @param cloudFolder 
	 * 			The folder used to bootstrap to the cloud.
	 * @return
	 * 			The private key used for starting the remote machines
	 * @throws IOException
	 */
	public static String getCloudPrivateKey(File cloudFolder) throws IOException {
		File pemFile = getPemFile(cloudFolder);
		if (pemFile == null) {
			return null;
		}
		return FileUtils.readFileToString(pemFile);
	}

   
	/**
	 * Create an HP cloud context.
	 * @param project HP cloud username.
	 * @param key HP cloud API key.
	 * @return the HP lClouds compute context.
     *
     * TODO : unify this with {@link beans.ServerBootstrapperImpl.NovaContext}
     *
	 */
	public static ComputeServiceContext createJcloudsContext(String project, String key, String secretKey ) {
		CloudBootstrapConfiguration cloudConf = ApplicationContext.get().conf().server.cloudBootstrap;
		ComputeServiceContext context;
		Properties overrides = new Properties();
		overrides.put("jclouds.keystone.credential-type", "apiAccessKeyCredentials");
		context = ContextBuilder.newBuilder( cloudConf.cloudProvider )
				.credentials( project + ":" + key, secretKey )
				.overrides(overrides)
				.buildView(ComputeServiceContext.class);
		return context;
	}

	private static File getPemFile(File cloudFolder) {
		final CloudBootstrapConfiguration cloudConf = ApplicationContext.get().conf().server.cloudBootstrap;
		File uploadDir = new File(cloudFolder, cloudConf.cloudifyHpUploadDirName);
		File[] filesList = uploadDir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {

				return name.startsWith(cloudConf.keyPairName)
						&& name.endsWith( "pem" );
			}
		});

		if ( filesList.length == 0 || filesList.length > 1) {
			return null;
		}
		return filesList[0];
	}
}
