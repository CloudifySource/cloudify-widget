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
package models;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import controllers.WidgetAdmin;

/**
 * The class provides ability to add any generic attribute with key/value that will serve as summary in Cloudify admin dashboard.
 * 
 * @author Igor Goldenberg
 * @see WidgetAdmin
 */
@XStreamAlias("summary")
public class Summary
{
  @XStreamAsAttribute
   private List<EntryAttributes> attributes;

  public Summary()
  {
	  attributes = new ArrayList<EntryAttributes>();
  }
  
  public void addAttribute( String key, String value )
  {
	  attributes.add( new EntryAttributes(key, value) );
  }
  
  @XStreamAlias("attribute")
  final static private class EntryAttributes
  {
	  @SuppressWarnings("unused")
	  String name;
	  @SuppressWarnings("unused")
	  String value;
	  
	  EntryAttributes( String name, String value )
	  {
		this.name = name;
		this.value = value;
	  }
  }
}
