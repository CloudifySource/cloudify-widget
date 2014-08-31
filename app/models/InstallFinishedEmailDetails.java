package models;

import cloudify.widget.common.MandrillSender;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.ebean.Model;
import utils.StringUtils;
import utils.Utils;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 5/21/14
 * Time: 8:33 PM
 */
@Entity
public class InstallFinishedEmailDetails extends Model {


    private static Logger logger = LoggerFactory.getLogger(InstallFinishedEmailDetails.class);

    @Id
    private Long id = null;

    private boolean enabled = false;

    /**
     * templateName,
     * templateContent,
     * mandrillMessage
     */
    @Lob
    @JsonIgnore
    private String data;

    @OneToOne
    @JsonIgnore
    private Widget widget;


    public boolean hasMandrillDetails(){
        return !StringUtils.isEmptyOrSpaces(data);
    }



    @JsonProperty
    @Transient
    public MandrillSender.MandrillEmailDetails getMandrillDetails(){
        if ( hasMandrillDetails() ) {
            try {
                return Utils.getObjectMapper().readValue(data, MandrillSender.MandrillEmailDetails.class);
            } catch (Exception e) {
                logger.error("unable to getMandrillDetails", e);
            }
        }
        return null;
    }


    public void setMandrillDetails( MandrillSender.MandrillEmailDetails details ){
        if ( details == null ){
            data = null;
        }else{
            try {
                data = Utils.getObjectMapper().writeValueAsString(details);
            }catch(Exception e){
                logger.error("unable to save mandrill data",e);
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @JsonIgnore
    public Widget getWidget() {
        return widget;
    }

    public void setWidget(Widget widget) {
        this.widget = widget;
    }
}
