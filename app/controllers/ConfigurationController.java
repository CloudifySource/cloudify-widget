package controllers;

import models.ConfigurationModel;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.codehaus.jackson.JsonNode;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import server.ApplicationContext;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 8/24/14
 * Time: 4:28 PM
 */
public class ConfigurationController extends Controller {

    public static Result  apply(){
        try {
            ConfigurationModel.get().apply(ApplicationContext.get().conf());
        }catch(Exception e){
            return internalServerError(ExceptionUtils.getFullStackTrace(e));
        }
        return ok();
    }

    public static Result save(){
        ConfigurationModel configurationModel = ConfigurationModel.get();
        JsonNode jsonNode = request().body().asJson();
        configurationModel.data = Json.stringify(jsonNode);
        configurationModel.update();
        configurationModel.save();
        return ok();
    }

    public static Result get(){
        return ok( Json.parse(ConfigurationModel.get().data) );
    }
}
