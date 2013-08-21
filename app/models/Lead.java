package models;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import play.db.ebean.Model;
import play.libs.Json;
import server.ApplicationContext;
import tyrex.services.UUID;
import utils.StringUtils;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 8/20/13
 * Time: 12:18 PM
 */
@Entity
@Table(uniqueConstraints =
@UniqueConstraint(name="unique_email", columnNames =  {"owner_id","email"}))
public class Lead extends Model {

    @Id
    public Long id;


    public String email;

    @JsonIgnore
    @Lob
    public String extra;

    @JsonIgnore
    public String confirmationCode;

    public String uuid;

    @ManyToOne
    @JsonIgnore
    public User owner;

    public Boolean validated;

    public Long createdTimestamp = System.currentTimeMillis();

    public Long trialTimeoutTimestamp = null; // allow calculation manual override.. for default use NULL here.

    public static Finder<Long, Lead> find = new Finder<Long, Lead>(Long.class, Lead.class);

    // calculate when should we kill the widget
//    public Long getTimeoutTimestamp( ){
//        ApplicationContext.get().conf().settings.
//    }


    @JsonProperty
    public JsonNode getExtra(){
        if (StringUtils.isEmptyOrSpaces( extra )){
            return null;
        }else{
            return Json.parse(extra);
        }
    }


}
