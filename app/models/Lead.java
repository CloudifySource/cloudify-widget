package models;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import play.db.ebean.Model;
import play.libs.Json;
import server.ApplicationContext;
import utils.StringUtils;

import javax.persistence.*;
import java.util.List;

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

//    calculate when should we kill the widget
    public Long getLeadExtraTimeout(){
        long deltaTime = 0;
        if ( trialTimeoutTimestamp != null ){
            deltaTime = trialTimeoutTimestamp;
        }else{
            if ( validated ){
                deltaTime = ApplicationContext.get().conf().settings.timeoutValues.verified;
            }else{
                deltaTime = ApplicationContext.get().conf().settings.timeoutValues.registered;
            }
        }
        deltaTime = deltaTime -  ( System.currentTimeMillis() - createdTimestamp ) ; // remove the amount of time since creation.
        return Math.max( 0 , deltaTime );
    }


    @JsonProperty
    public JsonNode getExtra(){
        if (StringUtils.isEmptyOrSpaces( extra )){
            return null;
        }else{
            return Json.parse(extra);
        }
    }

    public static Lead findByOwnerIdAndConfirmationCode( User owner, Long id, String confirmationCode ){
        return Lead.find.where().eq("owner", owner).eq("id",id).eq("confirmationCode", confirmationCode).findUnique();
    }

    public static Lead findByOwnerAndEmail( User owner, String email ){
        return Lead.find.where().eq( "owner", owner).eq( "email" , email ).findUnique();
    }

    public static List<Lead> findAllByOwner( User owner ){
        return Lead.find.where().eq("owner", owner).findList();
    }

    public static Lead findByOwnerAndId(User owner, Long id) {
        return Lead.find.where().eq("owner", owner).eq("id", id).findUnique();
    }

    public Long getId() {
        return id;
    }

    public String toDebugString(){
        return "Lead{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", extra='" + extra + '\'' +
                ", confirmationCode='" + confirmationCode + '\'' +
                ", uuid='" + uuid + '\'' +
                ", validated=" + validated +
                ", createdTimestamp=" + createdTimestamp +
                ", trialTimeoutTimestamp=" + trialTimeoutTimestamp +
                '}';
    }


}
