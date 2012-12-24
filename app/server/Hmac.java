package server;

/**
 * User: guym
 * Date: 12/19/12
 * Time: 1:16 PM
 */
public interface Hmac {
    String sign( Object... objs );

    boolean compare( String hmac, Object... objs );
}
