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
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.modules.spring.Spring;
import server.ApplicationContext;
import server.Config;

/**
 * On system startup trigger event onStart or onStop.
 * 
 * @author Igor Goldenberg
 */
public class Global extends GlobalSettings
{
	@Override
	public void onStart(Application app)
	{
		// print cloudify configuration
		Logger.info( Config.print() );
		
	    ApplicationContext.getServerPool();
	}

	@Override
	public void onStop(Application app)
	{
		ApplicationContext.getServerBootstrapper().close();
	}
}
