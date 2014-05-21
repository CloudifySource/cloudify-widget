/**
 This service uses MailChimp to send email to users that their installation is ready.
 **/

var logger = require('log4js').getLogger('MandrillMailSender');


var mandrill = require('mandrill-api/mandrill');


/**
 * opts = {
 *      'apiKey' : '__apiKey',
 *      'templateName' : '__templateName' ,
 *      'data' : '__data',
 *      'to' : '__to'
 * }
 * @param opts
 */
function sendEmail( opts ){
    var mandrill_client = new mandrill.Mandrill(opts.apiKey);
    var template_name = opts.templateName;
    var template_content = opts.data;
    var message = {
        'to': opts.to
    };

    mandrill_client.messages.sendTemplate({'template_name': template_name, 'template_content': template_content, 'message': message}, function(result) {
        logger.info(result);
        /*
         [{
         'email': 'recipient.email@example.com',
         'status': 'sent',
         'reject_reason': 'hard-bounce',
         '_id': 'abc123abc123abc123abc123abc123'
         }]
         */
    }, function(e) {
        // Mandrill returns the error as an object with name and message keys
        console.log('A mandrill error occurred: ' + e.name + ' - ' + e.message);
        // A mandrill error occurred: Unknown_Subaccount - No subaccount exists with the id 'customer-123'
    });

    logger.info('after sending the email');
}


exports.sendEmail = function ( opts , callback ){
    try {
        logger.info('sending emails', opts);
        sendEmail( opts );
    }catch(e){
        callback(e);
    }
};