package controllers;

import cloudify.widget.api.clouds.IWidgetLoginDetails;
import cloudify.widget.common.MailChimpWidgetLoginHandler;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.MessageException;
import org.openid4java.message.ax.FetchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.DynamicForm;
import play.mvc.Result;
import views.html.logins.google;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 3/10/14
 * Time: 11:00 PM
 */
public class WidgetGoogleLoginController extends AbstractLoginController {

    private static Logger logger = LoggerFactory.getLogger(WidgetGoogleLoginController.class);


    public static Result loginWithGoogle() {
        logger.info("loggging into google with openId4Java");
        return openId4JavaGoogleLogin();
//       return getPlayLoginWithGoogle();
    }


    private static Result openId4JavaGoogleLogin() {
        String redirectUrl = new MyOpenId("https://www.google.com/accounts/o8/id", routes.WidgetGoogleLoginController.googleLoginCallback().absoluteURL(request())).attributes().addEmail().apply().getReirectUrl();
        return redirect(redirectUrl);

    }

    private static class MyOpenId {

        private final DiscoveryInformation associate;
        private final AuthRequest authenticate;
        ConsumerManager manager = new ConsumerManager();

        private MyOpenId(String openId, String returnUrl) {
            try {
                associate = manager.associate(manager.discover(openId));
                authenticate = manager.authenticate(associate, returnUrl);
            } catch (Exception e) {
                throw new RuntimeException("unable to instantiate MyOpenId", e);
            }
        }

        public MyOpenId addExtension(FetchRequest fetchRequest) {
            try {
                authenticate.addExtension(fetchRequest);
            } catch (Exception e) {
                throw new RuntimeException("unable to add extension");
            }
            return this;
        }

        public Attributes attributes() {
            return new Attributes(this);
        }

        public String getReirectUrl() {
            return authenticate.getDestinationUrl(true);
        }

        public static class Attributes {
            private final MyOpenId myOpenId;
            FetchRequest fetchRequest = FetchRequest.createFetchRequest();

            public Attributes(MyOpenId myOpenId) {
                this.myOpenId = myOpenId;
            }

            public MyOpenId apply() {
                return this.myOpenId.addExtension(fetchRequest);
            }

            public Attributes addEmail() {
                try {
                    fetchRequest.addAttribute("email", "http://schema.openid.net/contact/email", true);
                    fetchRequest.addAttribute("firstname", "http://schema.openid.net/namePerson/first", true);
                    fetchRequest.addAttribute("lastname", "http://schema.openid.net/namePerson/last", true);
                } catch (MessageException e) {
                    throw new RuntimeException("unable to add email as attribute");
                }
                return this;
            }
        }

    }


    public static Result googleLoginCallback() {
//     {openid.op_endpoint=https://www.google.com/accounts/o8/ud, openid.signed=op_endpoint,claimed_id,identity,return_to,response_nonce,assoc_handle,ns.ext1,ext1.mode,ext1.type.email,ext1.value.email, openid.ns.ext1=http://openid.net/srv/ax/1.0, openid.sig=D8v0yywzchLsxChBA4/cdWJ/8jQzGfTNcHWBOWpzk50=, openid.response_nonce=2013-03-19T06:16:50ZcZqbrl75NjFBiw, openid.claimed_id=https://www.google.com/accounts/o8/id?id=AItOawkok_bxLaOJ341SwKtr9GtIcEgftGxXGoE, openid.assoc_handle=1.AMlYA9XC0Fq0Zld9fEUiCXrEOboBBt6b3iNI0nvdpX1gOvhdUGFlMJvbwhmfaxOc098KzxjxR51DFg, openid.ext1.value.email=some.email@gmail.com, openid.ns=http://specs.openid.net/auth/2.0, openid.identity=https://www.google.com/accounts/o8/id?id=AItOawkok_bxLaOJ341SwKtr9GtIcEgftGxXGoE, openid.ext1.type.email=http://schema.openid.net/contact/email, openid.mode=id_res, openid.ext1.mode=fetch_response, openid.return_to=http://localhost:9000/demos/googleLoginCallback}

        return getOpenid4jGoogleLoginCallback();
//        return getPlayGoogleLoginCallback();
    }

    private static Result getOpenid4jGoogleLoginCallback() {
        try {
            DynamicForm df = new DynamicForm().bindFromRequest();
            String openId = ((String) df.get("openid.identity")).split("\\?id=")[1];
            String email = df.get("openid.ext1.value.email");
            String firstname = df.get("openid.ext1.value.firstname");
            String lastname = df.get("openid.ext1.value.lastname");

            logger.info("user successfully logged in with [{},{}]", openId, email);


            GoogleLoginDetails loginDetails = new GoogleLoginDetails();
            loginDetails.setEmail(email);
            loginDetails.setFirstName(firstname);
            loginDetails.setLastName(lastname);
            handleLogin( loginDetails );


            return ok(google.render(openId, email));
        } catch (RuntimeException e) {
            logger.error("login was unsuccessful", e);
        }
        return badRequest("Invalid login");
    }


    public static class GoogleLoginDetails implements MailChimpWidgetLoginHandler.MailChimpLoginDetails {
        public String email;
        public String firstName;
        public String lastName;

        @Override
        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    }


}
