package beans;

import models.ServerNode;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import server.ProcExecutor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

/**
 * ****************************************************************************
 * Copyright (c) 2010 GigaSpaces Technologies Ltd. All rights reserved
 * <p/>
 * The software source code is proprietary and confidential information of GigaSpaces.
 * You may use the software source code solely under the terms and limitations of
 * The license agreement granted to you by GigaSpaces.
 * User: guym
 * Date: 12/3/12
 * Time: 8:10 PM
 * *****************************************************************************
 */
public class ProcExecutorImpl extends DefaultExecutor implements ProcExecutor {
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
