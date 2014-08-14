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
import java.util.*;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.apache.commons.io.FileUtils;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.NovaAsyncApi;
import org.jclouds.openstack.nova.v2_0.domain.Ingress;
import org.jclouds.openstack.nova.v2_0.domain.IpProtocol;
import org.jclouds.openstack.nova.v2_0.domain.KeyPair;
import org.jclouds.openstack.nova.v2_0.domain.SecurityGroup;
import org.jclouds.openstack.nova.v2_0.extensions.KeyPairApi;
import org.jclouds.openstack.nova.v2_0.extensions.SecurityGroupApi;
import org.jclouds.rest.RestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import server.ApplicationContext;
import beans.config.ServerConfig;
import beans.config.ServerConfig.CloudBootstrapConfiguration;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;


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
