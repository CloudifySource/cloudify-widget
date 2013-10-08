package models.query;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 6/20/13
 * Time: 12:27 AM
 */
public abstract class QueryConf<T extends QueryConf.Criteria> {
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

    protected abstract   T newCriteria();

    public abstract class Criteria{
        private QueryConf<T> conf;

        public void setQueryConf(QueryConf<T> conf) {
            this.conf = conf;
        }

        public QueryConf<T> done(){
            return conf;
        }
    }
}
