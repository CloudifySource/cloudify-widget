/*
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * <p/>
 * The software source code is proprietary and confidential information of GigaSpaces.
 * You may use the software source code solely under the terms and limitations of
 * the license agreement granted to you by GigaSpaces.
 */

package beans.config;

import com.google.common.base.Predicates;
import org.apache.commons.lang3.StringUtils;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Configuration;
import play.Play;
import play.libs.Time;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: guym
 * Date: 12/13/12
 * Time: 1:33 PM
 * <p/>
 * this class will auto wire pojos to configuration.
 * I am currently using reflection and play configuration only.
 * There is no need for more
 */
public class ConfigBean {

    private static Logger logger = LoggerFactory.getLogger( ConfigBean.class );

    private static Map<Class, ConfigValueHandler> handlers = new HashMap<Class, ConfigValueHandler>();

    static {

        handlers.put( Integer.class, new IntegerHandler() );
        handlers.put( int.class, new IntegerHandler() );

        handlers.put( Boolean.class, new BooleanObjectHandler() );
        handlers.put( boolean.class, new BooleanHandler() );

        handlers.put( long.class, new TimeHandler() );
        handlers.put( Long.class, new TimeHandler() );

        handlers.put( String.class, new StringHandler() );

        handlers.put( File.class, new FileHandler() );


    }



    public Conf getConfiguration()
    {
        Conf root = new Conf();
        injectConfiguration( root, Play.application().configuration() );
        return root;
    }

    private abstract static class ConfigValueHandler<T> {
        public abstract T getValue( Configuration conf, String key );
    }

    private static class ListHandler extends ConfigValueHandler<List>{
        @Override
        public List getValue( Configuration conf, String key )
        {
            try{
                com.typesafe.config.Config typesafeConf = (( play.api.Configuration ) conf.getClass().getField( "conf" ).get( conf )).underlying();
                return typesafeConf.getAnyRefList( key );
            }catch(Exception e){
                throw new RuntimeException( "unable to get list for " + key );
            }
        }
    }

    private static class IntegerHandler extends ConfigValueHandler<Integer> {
        @Override
        public Integer getValue( Configuration conf, String key )
        {
            return conf.getInt( key );
        }
    }

    private static class BooleanHandler extends ConfigValueHandler<Boolean> {
        @Override
        public Boolean getValue( Configuration conf, String key )
        {
            Boolean result = conf.getBoolean( key );
            return result ;
        }
    }

    private static class BooleanObjectHandler extends ConfigValueHandler<Boolean> {
           @Override
           public Boolean getValue( Configuration conf, String key )
           {
               return conf.getBoolean( key );
           }
       }

    public static class StringHandler extends ConfigValueHandler<String> {
        @Override
        public String getValue( Configuration conf, String key )
        {
            return conf.getString( key );
        }
    }

    public static class FileHandler extends ConfigValueHandler<File> {
        @Override
        public File getValue( Configuration conf, String key )
        {
            File file = Play.application().getFile( conf.getString( key ) );
            if ( !file.exists() ) {
                logger.warn( "file {} does not exists but required by the configuration", file.getAbsolutePath() );
            }
            return file;
        }
    }


    public static class TimeHandler extends ConfigValueHandler<Long> {
        @Override
        public Long getValue( Configuration conf, String key )
        {
            return Time.parseDuration( conf.getString( key ) ) * 1000L;
        }
    }

    private boolean isIgnoreNullValue( Config configAnn ){
        return configAnn == null || configAnn.ignoreNullValues();
    }

    private void injectConfiguration( Object obj, Configuration conf )
    {
        Set<Field> allFields = ReflectionUtils.getAllFields( obj.getClass(), Predicates.alwaysTrue() );
        for ( Field field : allFields ) {
            String configKey = field.getName();
            Config configAnn = null;
            if ( field.isAnnotationPresent( Config.class ) ) {
                configAnn = field.getAnnotation( Config.class );
                String playKey = configAnn.playKey();
                // use the annotated information only if not empty.
                configKey = StringUtils.isEmpty( playKey ) ? configKey : playKey;
            }

            if ( handlers.containsKey( field.getType() ) ) {
                try {
                    Object value = handlers.get( field.getType() ).getValue( conf, configKey );
                    if ( value != null || !isIgnoreNullValue( configAnn ) ){
                        field.set( obj, value );
                    }
                } catch ( Exception e ) {
                    logger.error( "unable to set value",e );
                }
            } else { // this is probably an Object. need to instantiate
                try {
                    if ( conf.getConfig( configKey ) != null ) {

                        // important : we assume the field is not null.
                        // this way we will be able to refresh configuration on command.
                        Object value = field.get( obj );
                        injectConfiguration( value, conf.getConfig( configKey ) );
                    }
                } catch ( Exception e ) {
                    throw new RuntimeException( String.format( "unable to populate configuration for key %s.%s", obj.getClass(), field.getName()), e );
                }
            }

            ConfigValueHandler handler = handlers.containsKey( field.getType() ) ? handlers.get( field.getType() ) : handlers.get( Configuration.class );

        }
    }
}
