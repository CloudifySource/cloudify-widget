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

package beans.cloudify;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.WS;

/**
 * User: guym
 * Date: 4/24/13
 * Time: 12:38 PM
 *
 * Implements REST calls to cloudify
 */
public class CloudifyRestClient {

    public String TEST_REST_FORMAT = "http://%s:8100/service/testrest";
    public String GET_REST_VERSION_FORMAT = "http://%s:8100/service/testrest"; // we are using the test rest error message to parse the version
    public String LIST_APPLICATIONS_FORMAT = "http://%s:8100/service/applications";
    public String LIST_SERVICES_FORMAT = "http://%s:8100/service/applications/%s/services";
    public String DESCRIBE_SERVICES_FORMAT = "http://%s:8100/service/applications/%s/services/description";
    public String GET_PUBLIC_IP_FORMAT= "http://%s:8100/admin/ProcessingUnits/Names/%s.%s/Instances/0/ServiceDetailsByServiceId/USM/Attributes/Cloud%%20Public%%20IP";

    private static Logger logger = LoggerFactory.getLogger( CloudifyRestClient.class );

    public CloudifyRestResult.TestRest testRest( String ip )
    {
        return getResult( CloudifyRestResult.TestRest.class, TEST_REST_FORMAT, ip  );
    }

    private String getUrl( String pattern, String ... args){
        String f = String.format( pattern, args );
        logger.info( "executing [{}]", f );
        return f;
    }

    public CloudifyRestResult.GetVersion getVersion( String ip )
    {
        return parse( CloudifyRestResult.GetVersion.class, getBody( WS.url( getUrl( GET_REST_VERSION_FORMAT, ip ) ).setHeader( "cloudify-api-version", CloudifyRestResult.GetVersion.DUMMY_VERSION ) )  );
    }

    public CloudifyRestResult.ListApplications listApplications( String ip ){
        return getResult( CloudifyRestResult.ListApplications.class, LIST_APPLICATIONS_FORMAT, ip );
    }

    public CloudifyRestResult.ListServices listServices( String ip, String application ){
        return getResult( CloudifyRestResult.ListServices.class, LIST_SERVICES_FORMAT, ip, application );
    }

    public CloudifyRestResult.GetPublicIpResult getPublicIp( String ip, String application, String service ){
        try{
            return getResult( CloudifyRestResult.GetPublicIpResult.class, GET_PUBLIC_IP_FORMAT, ip, application, service );
        }catch(Exception e){
            logger.error("could not get public IP. please make sure the application.service combination is correct [{}.{}]", application, service );
        }
        return new CloudifyRestResult.GetPublicIpResult();
    }

    private <T> T getResult ( Class<T>  clzz, String format, String ... args ){
        return parse(clzz, getBody( format, args ));
    }

    private String getBody( String format, String ... args ){
        return getBody( WS.url( getUrl( format, args ) ) );
    }

    private String getBody(WS.WSRequestHolder requestHolder ){
        return requestHolder.get().get().getBody();
    }

    private <T> T parse( Class<T> clzz, String body ){
        try{
        if ( org.apache.commons.lang3.StringUtils.isEmpty( body ) ) {
            logger.info( "body is empty" );
            return null;
        } else {
            ObjectMapper mapper = new ObjectMapper(  );
            return mapper.readValue( body, clzz );
        }
        }catch(Exception e){
            logger.error( "problems parsing class [{}] json [{}] ", new Object[]{clzz, body, e} );
            throw new RuntimeException( String.format("problems parsing class [%s] json [%s]", clzz, body), e );
        }

    }



}
