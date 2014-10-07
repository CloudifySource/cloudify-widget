/*
 * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

    public static String wrapWithQuotes( String s ){
        return "\"" + s + "\"";
    }

    // true iff any of the strings is empty
    public static boolean isAnyEmptyOrSpaces( String ... str ){
        for (String s : str) {
            if ( isEmptyOrSpaces(s)){
                return true;
            }
        }
        return false;
    }

}
