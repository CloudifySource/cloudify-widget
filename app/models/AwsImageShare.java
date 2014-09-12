package models;

import org.codehaus.jackson.annotate.JsonIgnore;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 8/25/14
 * Time: 6:49 PM
 */
@Entity
public class AwsImageShare extends Model{

    @Id
    private Long id;

    private String imageId;
    private String apiKey;
    private String apiSecretKey;
    private String endpoint;

    @JsonIgnore
    @OneToOne
    private Widget widget;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiSecretKey() {
        return apiSecretKey;
    }

    public void setApiSecretKey(String apiSecretKey) {
        this.apiSecretKey = apiSecretKey;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @JsonIgnore
    public Widget getWidget() {
        return widget;
    }

    public void setWidget(Widget widget) {
        this.widget = widget;
    }
}
