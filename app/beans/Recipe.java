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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import play.i18n.Messages;
import server.exceptions.ServerException;
import utils.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * User: guym
 * Date: 1/28/13
 * Time: 3:06 PM
 */
public class Recipe {

    private File recipeRootDirectory;
    private File recipeGroovyFile;

    public Recipe(File recipeFile) {
        this.recipeRootDirectory = recipeFile;
    }

    public static enum Type {
        APPLICATION("install-application", "application.groovy"), SERVICE("install-service", "service.groovy");


        static Type getRecipeTypeByFileName(String fileName) {
            for (Type type : values()) {
                if (fileName.endsWith(type.fileIdentifier)){
                    return type;
                }
            }
            return null;
        }


        String commandParam;
        String fileIdentifier;

        Type(String commandParam, String fileIdentifier) {
            this.commandParam = commandParam;
            this.fileIdentifier = fileIdentifier;
        }
    }

    private static WildcardFileFilter fileFilter = null;
    static {
        List<String> wildCards = new LinkedList<String>();
        for (Type type : Type.values()) {
            wildCards.add("*" + type.fileIdentifier);
        }
        fileFilter = new WildcardFileFilter( wildCards );
    }
    private static List<String> wildCards = new LinkedList<String>();

    /**
     * @return recipe type Application or Service by recipe directory.
     * @throws server.exceptions.ServerException
     *          if found a not valid recipe file.
     */
    public Type getRecipeType() {

        Collection<File> files = FileUtils.listFiles(recipeRootDirectory, fileFilter, null);

        if (CollectionUtils.isEmpty( files ) ) {
            throw new ServerException("could not find recipe groovy file.");
        }

        if ( CollectionUtils.size( files ) > 1) {
            throw new ServerException("found multiple recipe groovy files.");
        }

        File filename = CollectionUtils.first(files);
        recipeGroovyFile = filename;
        return filename == null ? null : Type.getRecipeTypeByFileName(filename.getName());
    }

    public File getPropertiesFile(){
        File propertiesFile = new File(recipeGroovyFile.getAbsolutePath().replace(".groovy", ".properties"));
        if ( !propertiesFile.exists() ){
            try {
                propertiesFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("unable to create properties file",e);
            }
        }
        return propertiesFile;
    }


}
