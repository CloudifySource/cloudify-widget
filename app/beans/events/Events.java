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

package beans.events;

import models.Widget;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.EventMonitor;

/**
 * User: guym
 * Date: 1/14/13
 * Time: 3:37 PM
 */
public class Events implements EventMonitor.Event {

    private String distinctId;
    private EventType type;
    private JSONObject jsonObject = new JSONObject(  );

    private static Logger logger = LoggerFactory.getLogger( Events.class );

    public static enum EventType{
        PLAY_WIDGET, STOP_WIDGET
    }

    protected static class WidgetEvent extends Events{
        public WidgetEvent(String remoteIp, Widget widget, EventType type ){

            super( remoteIp + "_" + widget.getApiKey(), type );
            populateWidgetProperties( widget, this );
            setProperty( "ip" , remoteIp );
        }
    }

    public static class PlayWidget extends WidgetEvent {
        public PlayWidget( String remoteIp, Widget widget )
        {
            super( remoteIp, widget, EventType.PLAY_WIDGET );
        }
    }

    public static class StopWidget extends WidgetEvent {
        public StopWidget( String remoteIp, Widget widget )
        {
            super( remoteIp, widget, EventType.STOP_WIDGET );
        }
    }



    protected static void populateWidgetProperties( Widget widget, Events events ){
        events.setProperty( "apiKey", widget.getApiKey() );
        events.setProperty( "productName", widget.getProductName() );
        events.setProperty( "widgetId", widget.getId().toString() );
    }

    protected Events( String distinctId, EventType type )
    {
        this.distinctId = distinctId;
        this.type = type;
    }

    @Override
    public String getDistinctId()
    {
        return distinctId;
    }

    @Override
    public String getName()
    {
        return type.toString();
    }

    @Override
    public JSONObject getProperties()
    {
        return jsonObject;
    }

    @Override
    public String asString()
    {
        return toString();
    }

    @Override
    public String toString()
    {
        return String.format( "Events{distinctId='%s', type=%s, jsonObject=%s}", distinctId, type, jsonObject );
    }

    protected void setProperty( String key, String value )
    {
        try {
            jsonObject.put( key, value );
        } catch ( Exception e ) {
            logger.error( String.format( "unable to set property [key, value]=[%s,%s]", key, value ), e );
        }
    }
}
