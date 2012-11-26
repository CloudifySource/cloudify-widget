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
package controllers;

import play.mvc.Controller;
import play.mvc.Results.Status;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;

public class RestUtils
{
	  public final static String OK_STATUS = "{\"status\":\"OK\"}";
 	  public final static String ERROR_STATUS = "{\"status\":\"error\",\"message\":\"%s\"}";

 	  
	  public static String toJSon( Object obj )
	  {
		  XStream xstream = new XStream( new JsonHierarchicalStreamDriver() ) 
		  {
		  	  @Override
		  	  protected MapperWrapper wrapMapper(MapperWrapper next) 
		  	  {
		  	    return new MapperWrapper(next) 
		  	    {
		  	      @SuppressWarnings("rawtypes")
		  	      @Override
		  	      public boolean shouldSerializeMember(Class definedIn, String fieldName) 
		  	      {
		  	        if (definedIn == play.db.ebean.Model.class) 
		  	          return false;
		  	        
		  	        return super.shouldSerializeMember(definedIn, fieldName);
		  	      }
		  	    };
		  	  }
		  	};
		  	
		    // xstream.setMode(XStream.NO_REFERENCES);
		  	xstream.autodetectAnnotations(true);

		  	String xml = xstream.toXML(obj);

		  	return xml;

	  }
	  
	  public static String toXml( Object obj )
	  {
		  XStream xstream = new XStream() {
		  	  @Override
		  	  protected MapperWrapper wrapMapper(MapperWrapper next) 
		  	  {
		  	    return new MapperWrapper(next) 
		  	    {
		  	      @SuppressWarnings("rawtypes")
		  	      @Override
		  	      public boolean shouldSerializeMember(Class definedIn, String fieldName) 
		  	      {
		  	        if (definedIn == play.db.ebean.Model.class)
		  	          return false;

		  	        return super.shouldSerializeMember(definedIn, fieldName);
		  	      }
		  	    };
		  	  }
		  	};
		  	
		  	xstream.autodetectAnnotations(true);

		  	String xml = xstream.toXML(obj);

		  	return xml;
	  }
	  
	  
		public static Status resultAsJson( Object obj )
		{
			String json = toJSon(obj);
			
			return Controller.ok(json).as("application/json");
		}

		public static Status resultErrorAsJson( String errorMsg )
		{
			return Controller.ok(String.format(ERROR_STATUS, errorMsg)).as("application/json");
		}
}