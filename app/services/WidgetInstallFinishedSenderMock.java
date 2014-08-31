package services;

import models.ServerNode;
import models.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 8/27/14
 * Time: 6:37 PM
 */
public class WidgetInstallFinishedSenderMock implements IWidgetInstallFinishedSender{
    private static Logger logger = LoggerFactory.getLogger(WidgetInstallFinishedSenderMock.class);

    @Override
    public void send(Widget widget, ServerNode serverNode ) {
        logger.info("mock sending email for finished installation");
    }
}
