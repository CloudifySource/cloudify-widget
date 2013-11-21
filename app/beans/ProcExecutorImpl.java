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
import java.io.IOException;
import java.util.Map;

import models.ServerNode;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;

import server.DeployManager;
import server.ProcExecutor;


/**
 * This class extends a {@link DefaultExecutor} and provides the ability to listen on the process output stream.
 * an Id property is saved for accessing the output from the play cache. 
 * 
 * @author Igor Goldenberg
 * @author Adaml
 * @see DeployManager
 */
public class ProcExecutorImpl extends DefaultExecutor implements ProcExecutor 
{
    private String id;

    
    public ProcExecutorImpl() { }

    public ProcExecutorImpl( ServerNode server, File recipe, String... args )
    {
        this.id = server.getId().toString();

    }
	
	@Override
    public String getId()
    {
        return id;
    }
	
	@Override
	public void setId(String id) {
		this.id = id;
	}

    @Override
    protected Process launch(CommandLine command, Map env, File dir) throws IOException {
        return super.launch(command, env, dir);
    }


}
