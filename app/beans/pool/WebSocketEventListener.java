package beans.pool;

import models.User;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.F;
import play.libs.Json;
import play.mvc.WebSocket;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 6/19/13
 * Time: 11:24 PM
 */
public class WebSocketEventListener implements PoolEventListener{

    private static Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    private User user;

    private WebSocket.In<String> in;
    private WebSocket.Out<String> out;

    private PoolEventManager manager;

    public WebSocketEventListener setManager(PoolEventManager manager) {
        this.manager = manager;
        return this;
    }

    public void listen(){
        logger.info("listening");
        manager.addListener(this);
        final WebSocketEventListener self = this;
        final PoolEventManager poolEventManager = manager;
        in.onMessage( new F.Callback<String>() {
            @Override
            public void invoke(String s) throws Throwable {
                logger.info("I am not responding to messages, however I got a message [{}]", s);
            }
        });
        in.onClose( new F.Callback0() {
            @Override
            public void invoke() throws Throwable {
                logger.info("not listening - connection closed");
                poolEventManager.removeListener(self);
            }
        });
        out.write("started listening");
    }

    public WebSocketEventListener setUser(User user) {
        this.user = user;
        return this;
    }

    public WebSocketEventListener setIn(WebSocket.In<String> in) {
        this.in = in;
        return this;
    }

    public WebSocketEventListener setOut(WebSocket.Out<String> out) {
        this.out = out;
        return this;
    }

    private boolean canSee( PoolEvent event ){
        return user.isAdmin() || ObjectUtils.equals(user, event.getUser() );
    }

    @Override
    public void handleEvent(PoolEvent poolEvent){
        logger.info("handling pool event [{}]", poolEvent);
        if ( canSee( poolEvent )){
            out.write( Json.stringify(Json.toJson(poolEvent)) );
        }
    }
}
