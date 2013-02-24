/*
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * <p/>
 * The software source code is proprietary and confidential information of GigaSpaces.
 * You may use the software source code solely under the terms and limitations of
 * the license agreement granted to you by GigaSpaces.
 */

package server;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;

import play.libs.Json;

/**
 * User: guym
 * Date: 12/27/12
 * Time: 10:41 AM
 *
 * Send GUI headers to show a message
 *
 */
public class HeaderMessage<T extends HeaderMessage> {

    protected static final String DISPLAY_MESSAGE = "display-message";
    @JsonProperty("msg")
    public String message;

    @JsonProperty("formErrors")
    public Map<String,String> formErrors = new HashMap<String,String>();


    protected String headerKey = DISPLAY_MESSAGE;

    public static enum Type{
        SUCCESS, ERROR
    }

    @JsonProperty("class")
    public Type type;

    public T setMessage( String message )
    {
        this.message = message;
        return (T) this;
    }

    private T _addFormError( String fieldName, String message ){
        formErrors.put(fieldName,message);
        return (T) this;
    }


    public T addFormError( String fieldName, String message ){
        return (T) setMessage( message ).setType( Type.ERROR )._addFormError( fieldName, message );
    }

    private T setHeaderKey( String value ){
        headerKey = value;
        return (T) this;
    }

    public T setSuccess( String message ){
        return (T) setMessage( message ).setType( Type.SUCCESS );
    }

    public T setError( String message ){
        return (T) setMessage( message ).setType( Type.ERROR );
    }

    public T setType( Type type )
    {
        this.type = type;
        return (T) this;
    }

    public String toJson(){
        return Json.toJson(this).toString();
    }
    public void apply( Map<String, String> headers ){
        headers.put( headerKey,toJson() );
    }
}
