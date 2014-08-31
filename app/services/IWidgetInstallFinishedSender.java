package services;

import cloudify.widget.allclouds.executiondata.ExecutionDataModel;
import models.ServerNode;
import models.Widget;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 8/27/14
 * Time: 6:36 PM
 */
public interface IWidgetInstallFinishedSender  {

    public void send( Widget widget, ServerNode serverNode );
}
