package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
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
public class CreateMachineOutputController extends GsController{

    private static Logger logger = LoggerFactory.getLogger(CreateMachineOutputController.class);

    static public Result index(){
        validateSession();
        try {
            return ok(Json.toJson(CreateMachineOutput.finder.all()));
        }catch(Exception e){
            logger.info("unable to find create machine errors",e);
            return internalServerError(e.getMessage());
        }

    }


    /**
     *
     * this function is used for monitoring.
     * just give me one that was unread so far, and mark it as read
     *
     * @return
     */
    static public Result readFirstUnread( ){
        boolean sendingAlert = false;
        if ( request().queryString().containsKey("sendingAlert") ){
            sendingAlert = "true".equals(request().queryString().get("sendingAlert")[0]);
        }
        List<CreateMachineOutput> output = CreateMachineOutput.finder.where().eq("alertWasSent", Boolean.FALSE).orderBy("created asc").setMaxRows(1).findList();
        if ( CollectionUtils.size(output) == 0){
            return notFound();
        }else{
            CreateMachineOutput first = CollectionUtils.first(output);
            first.setAlertWasSent(sendingAlert);
            first.save();
            return ok(Json.toJson(first));
        }
    }

    static public Result delete( Long outputId ){
        validateSession();
        try {
            CreateMachineOutput.finder.byId(outputId).delete();
        }catch(Exception e){

            logger.error("unable to delete error :: " + outputId, e);
            return internalServerError(e.getMessage());
        }
        return ok();
    }

    static public Result getException( Long outputId ){
        validateHmac();
        CreateMachineOutput createMachineOutput = CreateMachineOutput.finder.byId(outputId);
        return ok( createMachineOutput.getException() );
    }

    static public Result getOutput( Long outputId ){
        validateHmac();
        CreateMachineOutput createMachineOutput = CreateMachineOutput.finder.byId(outputId);
        return ok(createMachineOutput.getOutput());
    }

    static public Result deleteAll(){
        validateSession();
        try {
            Ebean.delete(CreateMachineOutput.finder.all());
        }catch(Exception e){
            logger.error("unable to delete all errors", e);
            return internalServerError(e.getMessage());
        }
        return ok();
    }


    static public Result markAllRead(){
        validateSession();
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
        validateSession();
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
