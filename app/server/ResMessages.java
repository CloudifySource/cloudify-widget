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
package server;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;


/**
 * Resource bundle of String messages. All messages loads by default from /conf/messages.conf file.
 * 
 * @author Igor Goldenberg
 */
public class ResMessages
{
	static Properties messages = new Properties();

	static
	{
		try {
			
			messages.load(new FileInputStream( Config.MESSAGES_CONFIG_FILE ));
			
		} catch (FileNotFoundException e) {
			throw new ServerException(e.getMessage());
		} catch (IOException e) {
			throw new ServerException("Failed to load a messages config file", e);
		}
	}

	/** prevent instantiation */
	private ResMessages() {}

	
	public static String getString(String key)
	{
		return messages.getProperty(key, "!" + key + "!");
	}
	
	public static String getFormattedString(String key, Object...args )
	{
		String value = messages.getProperty(key, "!" + key + "!");

		return String.format(value, args);
	}
}
