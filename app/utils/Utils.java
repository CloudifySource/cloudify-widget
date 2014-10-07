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

import com.microtripit.mandrillapp.lutung.view.MandrillMessage;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;
import play.Play;
import play.api.Configuration;
import play.data.validation.Validation;
import play.libs.Time;
import play.mvc.Http;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This class provides different static utility methods.
 * 
 * @author Igor Goldenberg
 */
public class Utils
{
	public static void threadSleep( long time )
	{
		try
		{
			Thread.sleep(time);
		} catch (InterruptedException e) {}
	}

	public static void addAllTrimmed( Collection<String> result, String[] values ){
		if ( !CollectionUtils.isEmpty( values )){
			for ( String value : values ) {
				if ( !StringUtils.isEmptyOrSpaces( value )){
					result.add( value.trim() );
				}
			}
		}
	}

	public static File getFileByRelativePath( String relativePath ){
		return Play.application().getFile( relativePath );
	}

	public static long parseTimeToMillis( String timeExpression ){
		return ((long)Time.parseDuration( timeExpression )) * 1000L ;
	}


	public static String requestToString( Http.RequestHeader requestHeader )
	{
		return requestHeader.toString();
	}

    public static Set<String> validate( Object obj ){
        Validator validator = Validation.getValidator();
        Set<ConstraintViolation<Object>> validate = validator.validate( obj );
        Set<String> errors = new HashSet<String>();
        for (ConstraintViolation<Object> widgetConstraintViolation : validate) {
            errors.add(widgetConstraintViolation.getMessage());
        }
        return errors;
    }



    public static void update( JsonNode json, Object obj ){
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readerForUpdating( obj ).treeToValue( json, obj.getClass() );
        } catch (Exception e) {
            throw new RuntimeException ( "unable to update model ",e);
        }
    }


    public static ObjectMapper getObjectMapper(){
        ObjectMapper mapper = new ObjectMapper();
        mapper.getSerializationConfig().addMixInAnnotations(MandrillMessage.class, MandrillMessageMixin.class);
        mapper.getDeserializationConfig().addMixInAnnotations(MandrillMessage.class, MandrillMessageMixin.class);
        return mapper;
    }



    private static class MandrillMessageMixin{
        @JsonIgnore
        public void setTags(String... tags) {

        }
    }

}