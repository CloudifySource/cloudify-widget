package models;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 5/21/14
 * Time: 8:33 PM
 */
@Entity
public class MandrillDetails extends Model {


    @Id
    private Long id = null;


    public String apiKey;
    public String templateName;

    public String csvBccEmails;

}
