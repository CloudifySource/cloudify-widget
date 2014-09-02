package controllers;

import com.avaje.ebean.Ebean;
import models.CreateMachineOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 9/2/14
 * Time: 12:36 AM
 */
public class CreateMachineOutputController extends Controller{

    private static Logger logger = LoggerFactory.getLogger(CreateMachineOutputController.class);

    static public Result index(){

        try {
            return ok(Json.toJson(CreateMachineOutput.finder.all()));
        }catch(Exception e){
            logger.info("unable to find create machine errors",e);
            return internalServerError(e.getMessage());
        }

    }


    static public Result delete( Long outputId ){
        try {
            CreateMachineOutput.finder.byId(outputId).delete();
        }catch(Exception e){

            logger.error("unable to delete error :: " + outputId, e);
            return internalServerError(e.getMessage());
        }
        return ok();
    }

    static public Result deleteAll(){
        try {
            Ebean.delete(CreateMachineOutput.finder.all());
        }catch(Exception e){
            logger.error("unable to delete all errors", e);
            return internalServerError(e.getMessage());
        }
        return ok();
    }


    static public Result markAllRead(){
        try{
            List<CreateMachineOutput> read = CreateMachineOutput.finder.where().eq("outputRead", Boolean.FALSE).findList();
            for (CreateMachineOutput createMachineOutput : read) {
                createMachineOutput.setOutputRead(true);
            }
            Ebean.save(read);
            Map<String, Integer> response = new HashMap<String, Integer>();
            response.put("updated", CollectionUtils.size(read));
            return ok(  Json.toJson(response));
        }catch(Exception e){
            logger.error("unable to mark all as read",e);
            return internalServerError(e.getMessage());
        }
    }

    static public Result countUnread(){
        try{
            int count = CreateMachineOutput.finder.where().eq("outputRead", Boolean.FALSE).findRowCount();
            Map<String, Integer> response = new HashMap<String, Integer>();
            response.put("result", count);
            return ok(Json.toJson(response));
        }catch(Exception e){
            logger.error("unable to count unread",e);
            return internalServerError(e.getMessage());
        }

    }
}
