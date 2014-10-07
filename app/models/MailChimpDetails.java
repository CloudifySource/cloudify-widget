package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 5/7/14
 * Time: 10:33 PM
 */
@Entity
public class MailChimpDetails extends Model {

    @Id
    private Long id;

    private String apiKey;

    private String listId;

    private boolean enabled;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


    @Override
    public String toString() {
        return "MailChimpDetails{" +
                "id=" + id +
                ", listId='" + listId + '\'' +
                ", enabled=" + enabled +
                "} " + super.toString();
    }
}
