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

/**
 * User: eliranm
 * Date: 4/25/13
 * Time: 5:06 PM
 */
public class BootstrapException extends Exception {
    public BootstrapException() {
        super();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public BootstrapException(String message) {
        super(message);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public BootstrapException(String message, Throwable cause) {
        super(message, cause);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public BootstrapException(Throwable cause) {
        super(cause);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
