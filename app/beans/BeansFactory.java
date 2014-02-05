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

import beans.config.Conf;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import server.EventMonitor;

import javax.inject.Inject;

/**
 * User: guym
 * Date: 2/27/13
 * Time: 9:57 AM
 */
public class BeansFactory {
    @Inject
    private Conf conf;

    @Inject
    private ApplicationContext applicationContext;

    private static Logger logger = LoggerFactory.getLogger( BeansFactory.class );

    @Inject
    private EventMonitor eventMonitorImpl;

    @Inject
    private EventMonitor eventMonitorMock;
//
    public EventMonitor getEventMonitor(){
//        logger.info( "bean factory - getEventMonitor is invoked with mock [{}] and impl [{}] ", eventMonitorMock, eventMonitorImpl );

        // GUY _ NOTE _ VERY IMPORTANT - the "@Inject"ed fields for eventMonitorImpl and eventMonitorMock are null.
        // please refer to stackoverflow's question : http://stackoverflow.com/questions/15183145/autowiring-factory-bean
        // to read more about this.
        if ( StringUtils.isEmpty(conf.mixpanelApiKey) ){
          logger.info( "using mock eventMonitor" );
            return ( EventMonitor ) applicationContext.getBean( "eventMonitorMock" );// eventMonitorMock;
        }else{
            logger.info( "using impl eventMonitor" );
            return ( EventMonitor ) applicationContext.getBean( "eventMonitorImpl" ); // eventMonitor
        }
    }


    public void setConf( Conf conf )
    {
        this.conf = conf;
    }

    public void setEventMonitorImpl( EventMonitor eventMonitorImpl )
    {
        this.eventMonitorImpl = eventMonitorImpl;
    }

    public void setEventMonitorMock( EventMonitor eventMonitorMock )
    {
        this.eventMonitorMock = eventMonitorMock;
    }
}
