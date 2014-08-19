package controllers;

import models.User;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import play.data.validation.Constraints;
import play.mvc.Controller;
import play.mvc.Result;
import server.ApplicationContext;
import server.HeaderMessage;
import server.exceptions.ServerException;
import utils.RestUtils;

import static utils.RestUtils.resultErrorAsJson;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 8/18/14
 * Time: 8:52 PM
 */
public class UserLoginController extends Controller {

//    /**
//     *
//     * this method will reset the user's password.
//     * the parameters are cryptic on purpose.
//     *
//     * @param p - the hmac
//     * @param pi - the user id
//     * @return -
//     */
//    public static Result resetPasswordAction( String p, Long pi ){
//        User user = User.findById( pi );
//        // validate p
//        if ( !ApplicationContext.get().getHmac().compare( p, user.getEmail(),  user.getId(), user.getPassword()  )){
//            return badRequest(  linkExpired.render() );
//        }
//        // if p is valid lets reset the password
//        String newPasswordStr = StringUtils.substring(p, 0, 7);
//        user.encryptAndSetPassword( newPasswordStr );
//        user.save();
//        return ok( newPassword.render( newPasswordStr ) );
//    }
//
//    public static Result postResetPassword( String email, String h ){
//        logger.info( "user {} requested password reset", email );
//        if ( !StringUtils.isEmpty( h ) ){
//            return badRequest(  ); // this is a bot.. lets block it.
//        }
//
//        if ( StringUtils.isEmpty( email ) || !(new Constraints.EmailValidator().isValid( email )) ){
//            new HeaderMessage().setError( "Invalid email" ).apply( response().getHeaders() );
//            return badRequest(  );
//        }
//
//        User user = User.find.where(  ).eq( "email",email ).findUnique();
//        if ( user == null ){
//            return ok(  ); // do not notify if user does not exist. this is a security breach..
//            // simply reply that an email was sent to the address.
//        }
//
//        ApplicationContext.get().getMailSender().resetPasswordMail( user );
//        return ok(  );
//    }

//    public static Result checkPasswordStrength( String password, String email ){
//        if ( !StringUtils.isEmpty( email  ) && new Constraints.EmailValidator().isValid( email )){
//            String result = isPasswordStrongEnough( password, email );
//            if ( result != null ){
//                new HeaderMessage().setError( result ).apply( response().getHeaders() );
//                return internalServerError(  );
//            }
//            return ok(  );
//        }
//        return ok(  );
//    }
//
//    private static String isPasswordStrongEnough( String password, String email ){
//        if ( StringUtils.length( password ) < 4 ){
//            return "Password is too short";
//        }
//        return null;
//    }
//
//    public static Result getPasswordMatch( String authToken, String password ){
//        User user = User.validateAuthToken( authToken );
//        String passwordWeakReason = isPasswordStrongEnough( password, user.getEmail() );
//        if ( passwordWeakReason == null ){
//            return ok( );
//        }
//        return ok( passwordWeakReason );
//    }
//
//    /**
//     *
//     * @param newPassword - the password user chose
//     * @param confirmPassword - the confirmed password
//     * @param email - user's email. used for checking similarity to password. passwords that are similar to email are considered weak.
//     * @return true iff password is considered strong enough according to our policy.
//     */
//    private static boolean validatePassword( String newPassword, String confirmPassword, String email )
//    {
//        if ( !StringUtils.equals( newPassword, confirmPassword ) ) {
//            new HeaderMessage().setError( "Passwords do not match" ).apply( response().getHeaders() );
//            return false;
//        }
//
//        String passwordWeakReason = isPasswordStrongEnough( newPassword, email );
//        if ( passwordWeakReason != null ) {
//            new HeaderMessage().setError( passwordWeakReason ).apply( response().getHeaders() );
//            return false;
//        }
//        return true;
//    }
//    public static Result postChangePassword(){
//        JsonNode parse = request().body().asJson();
//        String authToken = parse.get("authToken").getTextValue();
//        String oldPassword = parse.get("oldPassword").getTextValue();
//        String newPassword = parse.get("newPassword").getTextValue();
//        String confirmPassword = parse.get("confirmPassword").getTextValue();
//
//        User user = User.validateAuthToken( authToken );
//        if ( !user.comparePassword( oldPassword )){
//            new HeaderMessage().setError( "Wrong Password" ).apply( response().getHeaders() );
//            return internalServerError();
//        }
//
//        if ( !validatePassword( newPassword, confirmPassword, user.getEmail() ) ){
//            return internalServerError(  );
//        }
//
//
//        user.encryptAndSetPassword( newPassword );
//        user.save();
//        new HeaderMessage().setSuccess( "Password Changed Successfully" ).apply( response().getHeaders() );
//        return ok(  );
//    }


    /*
     * Creates new account.
     */
//    public static Result signUp( String email, String passwordConfirmation, String password, String firstname, String lastname )
//    {
//
//        if ( !ApplicationContext.get().conf().features.signup.on ){
//            return internalServerError("Instance does not support signup");
//        }
//
//        try {
//            Constraints.EmailValidator ev =  new Constraints.EmailValidator();
//            if ( StringUtils.isEmpty( email ) || !ev.isValid( email ) ){
//                new HeaderMessage().setError( "Email is incorrect" ).apply( response().getHeaders() );
//                return internalServerError(  );
//            }
//            if ( !validatePassword( password, passwordConfirmation, email ) ) {
//                return internalServerError();
//            }
//
//            User.Session session = User.newUser( firstname, lastname, email, password ).getSession();
//            return RestUtils.resultAsJson(session);
//        } catch ( ServerException ex ) {
//            return resultErrorAsJson( ex.getMessage() );
//        }
//    }


}
