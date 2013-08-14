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

package controllers;

import org.openid4java.consumer.ConsumerManager;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.MessageException;
import org.openid4java.message.ax.FetchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.DynamicForm;
import play.libs.OpenID;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.HashMap;
import java.util.Map;

/**
 * User: guym
 * Date: 1/25/13
 * Time: 4:50 PM
 */
public class Demos extends Controller {

    private static Logger logger = LoggerFactory.getLogger( Demos.class );

    public static Result loginWithGoogle(){
        logger.info("loggging into google with openId4Java");
       return openId4JavaGoogleLogin();
//       return getPlayLoginWithGoogle();
    }


    private static Result openId4JavaGoogleLogin(){
        String redirectUrl = new MyOpenId( "https://www.google.com/accounts/o8/id", routes.Demos.googleLoginCallback().absoluteURL( request() ) ).attributes().addEmail().apply().getReirectUrl();
        return redirect( redirectUrl );

    }

    private static class MyOpenId{

        private final DiscoveryInformation associate;
        private final AuthRequest authenticate;
        ConsumerManager manager = new ConsumerManager(  );

        private MyOpenId( String openId, String returnUrl )
        {
            try{
                associate = manager.associate( manager.discover( openId ) );
                authenticate = manager.authenticate( associate, returnUrl );
            }catch(Exception e){
                throw new RuntimeException( "unable to instantiate MyOpenId",e );
            }
        }

        public MyOpenId addExtension( FetchRequest fetchRequest ){
            try {
                authenticate.addExtension( fetchRequest );
            } catch ( Exception e ) {
                 throw new RuntimeException( "unable to add extension" );
            }
            return this;
        }

        public Attributes attributes(){
            return new Attributes(this);
        }

        public String getReirectUrl()
        {
            return authenticate.getDestinationUrl( true );
        }

        public static class Attributes{
            private final MyOpenId myOpenId;
            FetchRequest fetchRequest = FetchRequest.createFetchRequest(  );

            public Attributes( MyOpenId myOpenId )
            {
                this.myOpenId = myOpenId;
            }

            public MyOpenId apply(){
                return this.myOpenId.addExtension( fetchRequest );
            }

            public Attributes addEmail(){
                try {
                    fetchRequest.addAttribute( "email",  "http://schema.openid.net/contact/email", true );
                } catch ( MessageException e ) {
                    throw new RuntimeException( "unable to add email as attribute" );
                }
                return this;
            }
        }

    }

    private static Result getPlayLoginWithGoogle(){
        // http://stackoverflow.com/questions/9753702/openid-authentication-with-google-failing-randomly
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("email", "http://schema.openid.net/contact/email");
        String s = OpenID.redirectURL(
                "https://www.google.com/accounts/o8/id",
                routes.Demos.googleLoginCallback().absoluteURL( request() ),
                attributes
        ).get();
        return redirect(s);
    }



    public static Result googleLoginCallback(){
//     {openid.op_endpoint=https://www.google.com/accounts/o8/ud, openid.signed=op_endpoint,claimed_id,identity,return_to,response_nonce,assoc_handle,ns.ext1,ext1.mode,ext1.type.email,ext1.value.email, openid.ns.ext1=http://openid.net/srv/ax/1.0, openid.sig=D8v0yywzchLsxChBA4/cdWJ/8jQzGfTNcHWBOWpzk50=, openid.response_nonce=2013-03-19T06:16:50ZcZqbrl75NjFBiw, openid.claimed_id=https://www.google.com/accounts/o8/id?id=AItOawkok_bxLaOJ341SwKtr9GtIcEgftGxXGoE, openid.assoc_handle=1.AMlYA9XC0Fq0Zld9fEUiCXrEOboBBt6b3iNI0nvdpX1gOvhdUGFlMJvbwhmfaxOc098KzxjxR51DFg, openid.ext1.value.email=some.email@gmail.com, openid.ns=http://specs.openid.net/auth/2.0, openid.identity=https://www.google.com/accounts/o8/id?id=AItOawkok_bxLaOJ341SwKtr9GtIcEgftGxXGoE, openid.ext1.type.email=http://schema.openid.net/contact/email, openid.mode=id_res, openid.ext1.mode=fetch_response, openid.return_to=http://localhost:9000/demos/googleLoginCallback}

        return getOpenid4jGoogleLoginCallback();
//        return getPlayGoogleLoginCallback();
    }

    private static Result getOpenid4jGoogleLoginCallback(){
        try {
            DynamicForm df = new DynamicForm().bindFromRequest();
            String openId = (( String ) df.get( "openid.identity" )).split( "\\?id=" )[ 1 ];
            String email = df.get( "openid.ext1.value.email" );
            logger.info( "user successfully logged in with [{},{}]", openId, email );
            return ok( views.html.demos.loginResult.render( openId, email ) );
        } catch ( RuntimeException e ) {
            logger.error( "login was unsuccessful", e );
        }
        return badRequest( "Invalid login" );
    }

    private static Result getPlayGoogleLoginCallback()
    {
        OpenID.UserInfo userInfo = OpenID.verifiedId().get();
        return ok( views.html.demos.loginResult.render(userInfo.id.split("id=")[1], userInfo.attributes.get("email")));
    }

    public static Result validateUserIdFromLogin( String userId ){
        if ( "error".equals(userId )){
            return badRequest();
        }
        return ok();
    }

    public static Result getLoginDemoPage(){
        return ok(views.html.demos.login.render());
    }
}
