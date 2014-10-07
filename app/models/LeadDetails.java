package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 10/1/14
 * Time: 5:35 PM
 */
@Entity
public class LeadDetails extends Model{
    @Id
    public Long id;


    public Long created = System.currentTimeMillis();

    public boolean isRead = false;

    @Lob
    public String data;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
