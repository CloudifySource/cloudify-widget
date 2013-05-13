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

package beans;

import beans.api.ProcessStreamHandler;
import org.apache.commons.exec.PumpStreamHandler;
import server.ProcOutputStream;
import server.WriteEventListener;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * User: guym
 * Date: 1/29/13
 * Time: 2:52 PM
 */
public class ProcessStreamHandlerImpl extends PumpStreamHandler implements ProcessStreamHandler {

    private WriteEventListener writeEventListener;


    @Override
    protected void createProcessOutputPump(InputStream is, OutputStream os) {
        ProcOutputStream procOutputStream = createOutputStream();
        super.createProcessOutputPump(is, procOutputStream);
    }


    @Override
    protected void createProcessErrorPump(InputStream is, OutputStream os) {
        ProcOutputStream procOutputStream = createOutputStream();
        super.createProcessErrorPump(is, procOutputStream);
    }

    /**
     * enables the option to listen on the process output stream.
     *
     * @param wel see {@link WriteEventListener}
     */
    @Override
    public void setWriteEventListener(final WriteEventListener wel) {
        this.writeEventListener = wel;
    }

    private ProcOutputStream createOutputStream() {
        ProcOutputStream procOutputStream = new ProcOutputStream();
        procOutputStream.setProcEventListener(this.writeEventListener);
        return procOutputStream;
    }
}
