package utils;

import java.util.Collection;
import java.util.Collections;

/**
 * User: guym
 * Date: 12/20/12
 * Time: 12:52 PM
 */
public class CollectionUtils extends org.apache.commons.collections.CollectionUtils{

    // another flavor of "add all" array elements to collection.
    // this has the benefit of returning the list. so code is shorter.
    public static <T extends Collection> T addTo( Collection l, Object ... os ){
        Collections.addAll( l, os );
        return (T) l;
    }

    public static boolean isEmpty( String[] values )
    {
        return values == null || size ( values ) == 0 ;
    }

    public static int size( String[] vals ){
        return vals == null ? 0 : vals.length;
    }
}
