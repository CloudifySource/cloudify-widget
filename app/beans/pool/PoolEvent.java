package beans.pool;

import models.ServerNode;
import models.User;
import models.Widget;
import models.WidgetInstance;
import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.jclouds.openstack.v2_0.domain.Resource;
import tyrex.services.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 6/19/13
 * Time: 11:24 PM
 */
public class PoolEvent<T extends PoolEvent> {

    private  User user = null;
    public Type type;
    public String errorStackTrace;
    public String errorMessage;
    private long timestamp = System.currentTimeMillis();
    private Long id = null;

    public Long getId() {
        return id;
    }

    public T setId(Long id) {
        this.id = id;
        return (T) this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getCategory(){
        return getClass().getSimpleName();
    }

    public User getUser() {
        return user;
    }

    public T setErrorStackTrace(String errorStackTrace) {
        this.errorStackTrace = errorStackTrace;
        return (T) this;
    }

    public T setType(Type type) {
        this.type = type;
        return (T) this;
    }

    public PoolEvent setUser(User user) {
        this.user = user;
        return this;
    }


    public static enum Type{
        CREATE, DELETE, UPDATE, ERROR
    }

    public static class MachineStateEvent extends PoolEvent<MachineStateEvent>{
        public Resource resource;

        public MachineStateEvent setResource(Resource resource) {
            this.resource = resource;
            return this;
        }
    }

    public T setErrorMessage( String errorMessage){
        this.errorMessage = errorMessage;
        return (T) this;
    }

    public static class ServerNodeEvent extends PoolEvent<ServerNodeEvent>{
        public ServerNode serverNode;

        public ServerNodeEvent setServerNode(ServerNode serverNode) {
            this.serverNode = serverNode;
            return this;
        }


    }

    public static class WidgetInstanceEvent extends PoolEvent<WidgetInstanceEvent>{
        public Widget.Status status;
        public WidgetInstance widgetInstance;


        public WidgetInstanceEvent setStatus(Widget.Status status) {
            this.status = status;
            return this;
        }

        public WidgetInstanceEvent setWidgetInstance(WidgetInstance widgetInstance) {
            this.widgetInstance = widgetInstance;
            return this;
        }
    }
}
