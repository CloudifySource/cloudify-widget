package controllers;

import models.User;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import play.data.validation.Constraints;
import play.libs.Json;
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
public class UsersController extends GsController {

    public static Result getUserDetails(){
        User user = validateSession();
        return ok(Json.toJson(user));
    }

}
