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

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import server.EventMonitor;

import com.mixpanel.mixpanelapi.ClientDelivery;
import com.mixpanel.mixpanelapi.MessageBuilder;
import com.mixpanel.mixpanelapi.MixpanelAPI;

/**
 * User: guym
 * Date: 1/14/13
 * Time: 1:30 PM
 */
public class EventMonitorMixpanel implements EventMonitor {
    private String apiKey;
    private MessageBuilder builder;
    MixpanelAPI mixpanel = new MixpanelAPI();

    private static Logger logger = LoggerFactory.getLogger( EventMonitorMixpanel.class );


    public void init(){
        builder = new MessageBuilder(apiKey);
    }

    @Override
    public void eventFired( Event event )
    {
        try {
            logger.info( "sending [{}] event", event.getName() );
            JSONObject message = builder.event( event.getDistinctId(), event.getName(), event.getProperties() );
            ClientDelivery delivery = new ClientDelivery();
            delivery.addMessage( message );
            mixpanel.deliver( delivery );
        } catch ( Exception e ) {
            logger.error( String.format("Unable to send event to mixpanel : [%s]", event.toString()) ,e );
        }
    }

    @Override
    public void updateUser( UpdateUserEvent event )
    {
        builder.set( event.getUser().getId().toString(), event.getProperties() );
    }

    @Override
    public void auditUserAction( AuditActionEvent event )
    {
        builder.increment( event.getUser().getId().toString() , event.getProperties() );
    }

    public void setApiKey( String apiKey )
    {
        this.apiKey = apiKey;
    }
}
