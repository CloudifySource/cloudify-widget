package beans;

import beans.config.Conf;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import server.Hmac;
import utils.CollectionUtils;

import javax.inject.Inject;
import java.security.MessageDigest;
import java.util.LinkedList;
import java.util.List;

/**
 * User: guym
 * Date: 12/13/12
 * Time: 12:56 PM
 */
public class HmacImpl implements Hmac {

    public static final String SEPARATOR = "___";
    @Inject
    private Conf conf;

    @Override
    public String sign( Object... objs ){
        try{
            List objects = CollectionUtils.addTo( new LinkedList(), objs );
            objects.add( conf.application.secret ); // add the secret so no-one can reproduce this string.
            String joined = StringUtils.join( objs, SEPARATOR );
            byte[] base64Result = Base64.encodeBase64( joined.getBytes() );
            byte[] md5s = MessageDigest.getInstance( "MD5" ).digest( base64Result );

            StringBuffer sb = new StringBuffer();
            for ( byte md5 : md5s ) {
                sb.append( Integer.toString( (md5 & 0xff) + 0x100, 16 ).substring( 1 ) );
            }

            return sb.toString();
        }catch ( Exception e ){
            throw new RuntimeException( "unable to sign Hmac for " + ArrayUtils.toString( objs ),e );
        }
    }

    @Override
    public boolean compare( String hmac, Object... objs ){
        return StringUtils.equals( hmac, sign( objs ) );
    }

    public void setConf( Conf conf )
    {
        this.conf = conf;
    }
}
