package beans.pool;

import beans.config.Conf;
import models.PoolEventModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Akka;
import play.libs.Json;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 6/19/13
 * Time: 11:31 PM
 *
 * will manage subscriptions and publish
 *
 */
public class PoolEventManager implements PoolEventListener{
    private static Logger logger = LoggerFactory.getLogger(PoolEventManager.class);
    private List<PoolEventListener> listeners = new LinkedList<PoolEventListener>();

    @Inject
    private Conf conf;


    public void removeListener( PoolEventListener listener ){
        listeners.remove(listener);
    }

    public void addListener( PoolEventListener listener ){
        listeners.add( listener );
    }

    @Override
    public void handleEvent(final PoolEvent poolEvent) {

        if ( poolEvent.getId() == null ){ // initialize ID.
            PoolEventModel poolEventModel = new PoolEventModel();
            poolEventModel.save();
            poolEventModel.refresh();
            poolEvent.setId(poolEventModel.getId());
        }

            Akka.future(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                PoolEventModel model = PoolEventModel.find.byId( poolEvent.getId() );
                model.setEmpty(false);
                model.setEvent(Json.stringify(Json.toJson(poolEvent)));
                model.updateTime(poolEvent.getTimestamp());
                model.save();
                return null;
            }
        });


        for (PoolEventListener listener : listeners) {
            try{
                listener.handleEvent( poolEvent );
            }catch(Exception e){
                logger.error("listener threw exception",e);
            }
        }


    }

    public List<PoolEventModel> getEvents( int size, long before, long after ){
        return PoolEventModel.find.where().gt("lastUpdate", after).lt("lastUpdate",before ).setMaxRows(size).findList();
    }

    public void setConf(Conf conf) {
        this.conf = conf;
    }
}
