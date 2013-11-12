package models;

import play.db.ebean.Model;

import javax.persistence.*;
import java.util.List;

/**
 * User: guym
 * Date: 9/10/13
 * Time: 2:53 PM
 */
@Entity
public class UserPermissions extends Model {

    @Id
    private Long id;

    @OneToMany( mappedBy = "permissions" )
    private List<User> user;

    @Column
    // this permissions prevents hackers from snatching widget instances.
    // this is not a perfect solution as 2 users with this permission can still snatch each others' instances.
    private boolean canAssignLeads = false;

    @Override
    public String toString() {
        return "UserPermissions{" +
                "canAssignLeads=" + canAssignLeads +
                ", id=" + id +
                '}';
    }

    public boolean isCanAssignLeads() {
        return canAssignLeads;
    }

    public void setCanAssignLeads(boolean canAssignLeads) {
        this.canAssignLeads = canAssignLeads;
    }
}
