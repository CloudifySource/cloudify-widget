package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 5/21/14
 * Time: 3:23 PM
 *
 *
 * Details about the person running this widget instance.
 *
 * an optional information if decided by the user.
 *
 */
@Entity
public class WidgetInstanceUserDetails extends Model {

    public static String COOKIE_NAME = "instanceUserDetails";


    @Id
    public Long id = null;


    public String name;
    public String lastName;
    public String email;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
