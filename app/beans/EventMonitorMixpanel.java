/*
 * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
 * <p/>
 * The software source code is proprietary and confidential information of GigaSpaces.
 * You may use the software source code solely under the terms and limitations of
 * the license agreement granted to you by GigaSpaces.
 */

package beans;

import com.mixpanel.mixpanelapi.ClientDelivery;
import com.mixpanel.mixpanelapi.MessageBuilder;
import com.mixpanel.mixpanelapi.MixpanelAPI;
import org.apache.commons.lang.math.NumberUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.EventMonitor;

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
