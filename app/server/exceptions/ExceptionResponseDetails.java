/*
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * <p/>
 * The software source code is proprietary and confidential information of GigaSpaces.
 * You may use the software source code solely under the terms and limitations of
 * the license agreement granted to you by GigaSpaces.
 */

package server.exceptions;

import server.HeaderMessage;

/**
 * User: guym
 * Date: 12/26/12
 * Time: 9:00 AM
 *
 * This will allow us to pass header values regarding the error we have.
 * It will allow clients to properly detect which error happened without scraping the body.
 *
 * This is a prototype. We should probably spec this properly before using it massively.
 *
 */
public class ExceptionResponseDetails extends HeaderMessage<ExceptionResponseDetails>{


    private final ServerException e;

    public ExceptionResponseDetails( ServerException e )
    {
        this.e = e;
    }

    public String getHeaderKey()
    {
        return headerKey;
    }

    public ExceptionResponseDetails setHeaderKey( String headerKey )
    {
        this.headerKey = headerKey;
        return this;
    }

    // allows chains    throw new ServerException().response().setHeaderKey(..).done()
    public ServerException done(){
        return e;
    }
}
