package models.query;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Junction;
import play.db.ebean.Model;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 6/20/13
 * Time: 12:27 AM
 */
public abstract class QueryConf<T extends QueryConf.Criteria, M extends Model> {
    public int maxRows;
    public List<T> criterias = new LinkedList<T>();


    public QueryConf setMaxRows(int maxRows) {
        this.maxRows = maxRows;
        return this;
    }

    public T criteria(){
        T inst = newCriteria();
            inst.setQueryConf(this);
            criterias.add(inst);
            return inst;
    }

    abstract protected void applyCriteria( T criteria, Junction<M> conjunction );

    public ExpressionList<M> find( ){
        return apply( getFinder().where() );
    }

    abstract public Model.Finder<Long,M> getFinder();

    public ExpressionList<M> apply( ExpressionList<M> where ){
        Junction<M> disjunction = where.disjunction();
        for (T criteria : criterias) {
            Junction<M> conjunction = disjunction.conjunction();
            applyCriteria( criteria, conjunction );
        }
        if ( maxRows > 0 ){
            where.setMaxRows( maxRows );
        }
        return where;
    }

    protected abstract   T newCriteria();

    public abstract class Criteria{
        private QueryConf<T,M> conf;

        public void setQueryConf(QueryConf<T,M> conf) {
            this.conf = conf;
        }

        public QueryConf<T,M> done(){
            return conf;
        }
    }
}
