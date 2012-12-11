/*******************************************************************************
 * Copyright (c) 2011 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package beans;

import models.ServerNode;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

import server.DeployManager;
import server.ProcExecutor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;


/**
 * This class extends a {@link DefaultExecutor} and contains the server information where the recipe was deployed.
 * It also contains an output stream for the forked process. 
 * 
 * @author Igor Goldenberg
 * @see DeployManager
 */
public class ProcExecutorImpl extends DefaultExecutor implements ProcExecutor 
{
    private String id;
    private String publicIP;
    private String privateIP;
    private File recipe;
    private String[] args;
    private long expirationTime;
    private ProcessStreamHandler procHandler;

    final static class ProcessStreamHandler extends PumpStreamHandler
	 {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();

		@Override
		protected void createProcessOutputPump(InputStream is, OutputStream os)
		{
			super.createProcessOutputPump(is, baos);
		}

		public String getOutput()
		{
			return baos.toString();
		}
	 }

    public ProcExecutorImpl( ServerNode server, File recipe, String... args )
    {
        this.id = server.getId();

        this.publicIP = server.getPublicIP();
        this.privateIP = server.getPrivateIP();
        this.recipe = recipe;
        this.args = args;
        this.expirationTime = server.getExpirationTime();

        procHandler = new ProcessStreamHandler();
        setStreamHandler( procHandler );
    }

    public String getId()
    {
        return id;
    }

    public String getPublicServerIP()
    {
        return publicIP;
    }

    public String getPrivateServerIP()
    {
        return privateIP;
    }

    public File getRecipe()
    {
        return recipe;
    }

    public String[] getArgs()
    {
        return args;
    }

    public String getOutput()
    {
        return procHandler.getOutput();
    }

    public int getElapsedTimeMin()
    {
        long elapsedTime = expirationTime - System.currentTimeMillis();
        if ( elapsedTime <= 0 )
            return 0;
        else
            return ( int ) TimeUnit.MILLISECONDS.toMinutes( elapsedTime );
    }
}
