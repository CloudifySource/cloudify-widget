package models;

import play.db.ebean.Model;

import javax.persistence.*;

/**
 * User: eliranm
 * Date: 4/23/13
 * Time: 7:22 PM
 */
@Entity
public class ServerNodeEvent extends Model {

    @Id
    private Long id;

    public static enum Type{
        DONE, ERROR
    }

    @Enumerated(EnumType.STRING)
    private Type eventType;

    private Long eventTimestamp = System.currentTimeMillis();

    @Column(length = 512)
    private String msg = "";

    @ManyToOne
    private ServerNode serverNode;

    public ServerNodeEvent setEventType(Type type) {
        this.eventType = type;
        return this;
    }

    public ServerNodeEvent setEventTimestamp(Long timestamp) {
        this.eventTimestamp = timestamp;
        return this;
    }

    public ServerNodeEvent setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public ServerNodeEvent setServerNode(ServerNode serverNode) {
        this.serverNode = serverNode;
        return this;
    }

    public Long getId() {
        return id;
    }

    public Type getEventType() {
        return eventType;
    }

    public Long getEventTimestamp() {
        return eventTimestamp;
    }

    public String getMsg() {
        return msg;
    }

    public ServerNode getServerNode() {
        return serverNode;
    }
}
