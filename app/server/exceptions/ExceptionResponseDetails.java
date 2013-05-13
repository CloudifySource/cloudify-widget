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
