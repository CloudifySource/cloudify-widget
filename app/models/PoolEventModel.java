package models;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Junction;
import models.query.QueryConf;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Version;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 6/20/13
 * Time: 12:14 AM
 */
@SuppressWarnings("serial")
@Entity
public class PoolEventModel extends Model {

    @Id
    private Long id;

    private Long timestamp;

    private Long lastUpdate;

    @Lob
    private String event;

    private boolean empty = true;

    @Version
    private long version = 0;

    public static Finder<Long,PoolEventModel> find = new Finder<Long,PoolEventModel>(Long.class, PoolEventModel.class);

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }



    public Long getId() {
        return id;
    }


    public long getTimestamp() {
        return timestamp;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void updateTime( long timestamp ){
       if ( this.timestamp == null ){
           this.timestamp = timestamp;
       }
       lastUpdate = timestamp;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public boolean isEmpty() {
        return empty;
    }

    public static List<PoolEventModel> findByCriteria( QueryConfig queryConfig ){
        return queryConfig.find().findList();
    }

    public static class QueryConfig extends QueryConf<QueryConfig.PoolModelCriteria, PoolEventModel> {

        @Override
        protected PoolModelCriteria newCriteria() {
            return new PoolModelCriteria();
        }

        @Override
        public Finder<Long, PoolEventModel> getFinder() {
            return PoolEventModel.find;
        }

        @Override
        protected void applyCriteria(PoolModelCriteria criteria, Junction<PoolEventModel> conjunction) {
            conjunction.eq("1", "1");
            if ( criteria.getEmpty() != null ){
                conjunction.eq("empty", criteria.getEmpty());
            }

            if ( criteria.getLastUpdate() != null ){
                conjunction.eq("lastUpdate", criteria.lastUpdate );
            }

            if ( criteria.getTimestamp() != null ){
                conjunction.eq("timestamp", criteria.getTimestamp() );
            }

            if ( criteria.getNotNull() != null ){
                conjunction.isNotNull("event");
            }

            if ( criteria.getBeforeTimestamp() != null ){
                conjunction.lt("timestamp", criteria.getBeforeTimestamp());
            }

            if ( criteria.getAfterTimestamp() != null ){
                conjunction.ge("timestamp", criteria.getAfterTimestamp());
            }

            if ( criteria.getBeforeUpdate() != null ){
                conjunction.lt("lastUpdate", criteria.getBeforeUpdate() );
            }

            if ( criteria.getAfterUpdate() != null ){
                conjunction.ge("lastUpdate", criteria.getAfterUpdate());
            }
        }

        public class PoolModelCriteria extends QueryConf<PoolModelCriteria, PoolEventModel>.Criteria{
            private Boolean empty;
            private Long timestamp;
            private Long lastUpdate;
            private Boolean notNull;
            private Long beforeTimestamp;
            private Long afterTimestamp;
            private Long beforeUpdate;
            private Long afterUpdate;

            public Boolean getEmpty() {
                return empty;
            }

            public PoolModelCriteria setEmpty(Boolean empty) {
                this.empty = empty;
                return this;
            }

            public Long getTimestamp() {
                return timestamp;

            }

            public PoolModelCriteria setTimestamp(Long timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            public Long getLastUpdate() {
                return lastUpdate;
            }

            public PoolModelCriteria setLastUpdate(Long lastUpdate) {
                this.lastUpdate = lastUpdate;
                return this;
            }

            public Boolean getNotNull() {
                return notNull;
            }

            public PoolModelCriteria setNotNull(Boolean notNull) {
                this.notNull = notNull;
                return this;
            }

            public Long getBeforeTimestamp() {
                return beforeTimestamp;
            }

            public PoolModelCriteria setBeforeTimestamp(Long beforeTimestamp) {
                this.beforeTimestamp = beforeTimestamp;
                return this;
            }

            public Long getAfterTimestamp() {
                return afterTimestamp;
            }

            public PoolModelCriteria setAfterTimestamp(Long afterTimestamp) {
                this.afterTimestamp = afterTimestamp;
                return this;
            }

            public Long getBeforeUpdate() {
                return beforeUpdate;
            }

            public PoolModelCriteria setBeforeUpdate(Long beforeUpdate) {
                this.beforeUpdate = beforeUpdate;
                return this;
            }

            public Long getAfterUpdate() {
                return afterUpdate;
            }

            public PoolModelCriteria setAfterUpdate(Long afterUpdate) {
                this.afterUpdate = afterUpdate;
                return this;
            }
        }
    }


}
