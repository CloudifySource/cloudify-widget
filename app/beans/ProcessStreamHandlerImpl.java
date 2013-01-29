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
