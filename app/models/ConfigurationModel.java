package models;

import beans.config.Conf;
import cloudify.widget.common.CollectionUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 8/24/14
 * Time: 4:19 PM
 */
@Entity
public class ConfigurationModel extends Model {

    private static Logger logger = LoggerFactory.getLogger(ConfigurationModel.class);

    @Id
    public Long id;

    @Lob
    public String data;

    public static ConfigurationModel get(){
        Finder< Long, ConfigurationModel > find = new Finder< Long, ConfigurationModel >( Long.class, ConfigurationModel.class );
        List<ConfigurationModel> all = find.all();
        if (CollectionUtils.isEmpty(all)){
            ConfigurationModel configurationModel = new ConfigurationModel();
            configurationModel.save();
            return configurationModel;
        }

        if ( CollectionUtils.size( all ) > 1 ){
            throw new RuntimeException("more than one configuration model");
        }

        return CollectionUtils.first( all );
    }


    public void silentApply( Conf conf ){
        try {
            apply(conf);
        }catch(Exception e){
            logger.error("unable to apply configuration",e);
        }
    }

    public void apply( Conf conf ){
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.readerForUpdating(conf).treeToValue(mapper.readTree(data), Conf.class);
        }catch(Exception e){
            throw new RuntimeException("unable to apply changed",e);
        }
    }
}
