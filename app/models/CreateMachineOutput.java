package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 9/2/14
 * Time: 12:34 AM
 */
@Entity
public class CreateMachineOutput extends Model {

    @Id
    private Long id;

    @Lob
    private String content;

    private Long created;

    private boolean outputRead = false;


    public static Finder<Long, CreateMachineOutput> finder = new Finder<Long, CreateMachineOutput>( Long.class, CreateMachineOutput.class );

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isOutputRead() {
        return outputRead;
    }

    public void setOutputRead(boolean errorRead) {
        this.outputRead = errorRead;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }
}
