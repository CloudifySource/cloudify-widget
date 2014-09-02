package services;

import cloudify.widget.allclouds.executiondata.ExecutionDataModel;
import cloudify.widget.common.MandrillSender;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage;
import models.ServerNode;
import models.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 8/27/14
 * Time: 6:36 PM
 */
public class WidgetInstallFinishedSenderImpl implements IWidgetInstallFinishedSender {
    private static Logger logger = LoggerFactory.getLogger(WidgetInstallFinishedSenderImpl.class);
    @Override
    public void send(Widget widget, ServerNode serverNode ) {

        if (!widget.installFinishedEmailDetails.isEnabled() ){
            logger.info("email sending when install finished is disabled");
            return;
        }

        ExecutionDataModel executionDataModel = serverNode.getExecutionDataModel();

        if ( !executionDataModel.has(ExecutionDataModel.JsonKeys.LOGIN_DETAILS) ){
            logger.info("not sending email. execution model does not have login details");
            return;
        }

        ExecutionDataModel.LoginDetails loginDetails = executionDataModel.getLoginDetails();


        logger.info("sending install finished email to [{}]",loginDetails.email);
        if (widget.installFinishedEmailDetails == null || !widget.installFinishedEmailDetails.hasMandrillDetails() ){
            throw new RuntimeException("unable to send without details");
        }else{
            MandrillSender.MandrillEmailDetails mandrillDetails = widget.installFinishedEmailDetails.getMandrillDetails();

            mandrillDetails.templateContent = new HashMap<String, String>();

            MandrillMessage.Recipient recipient = new MandrillMessage.Recipient();
            recipient.setType(MandrillMessage.Recipient.Type.TO);
            recipient.setName(loginDetails.name);
            recipient.setEmail(loginDetails.email);


            if ( mandrillDetails.templateContent == null ){
                mandrillDetails.templateContent = new HashMap<String, String>();
            }

            // need to make sure this list is aligned with _login.html list -- our documentation -- all the time
            mandrillDetails.templateContent.put("username", serverNode.getExecutionDataModel().getLoginDetails().name );
            mandrillDetails.templateContent.put("randomValue", serverNode.getRandomPassword() );
            mandrillDetails.templateContent.put("publicIp", serverNode.getPublicIP() );


            if ( mandrillDetails.mandrillMessage == null ){
                mandrillDetails.mandrillMessage = new MandrillMessage();
            }

            if ( mandrillDetails.mandrillMessage.getTo() == null ){
                mandrillDetails.mandrillMessage.setTo( new LinkedList<MandrillMessage.Recipient>());
            }

            mandrillDetails.mandrillMessage.getTo().add(recipient);
            MandrillSender sender = new MandrillSender();
            sender.sendEmail(mandrillDetails);

        }
    }
}
