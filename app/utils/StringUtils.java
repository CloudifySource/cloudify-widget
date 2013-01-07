/*
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * <p/>
 * The software source code is proprietary and confidential information of GigaSpaces.
 * You may use the software source code solely under the terms and limitations of
 * the license agreement granted to you by GigaSpaces.
 */

package utils;

/**
 * User: guym
 * Date: 12/24/12
 * Time: 1:51 PM
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils {

    public static boolean isEmptyOrSpaces( String str ){
        return str == null || str.trim().isEmpty();
    }

    public static String dos2Unix( String str ){
        return replace( str, "\r\n", "\n");
    }

}
