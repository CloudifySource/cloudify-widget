



var path = require('path');
var conf = require('./../src/services/conf');

var logger = require('log4js').getLogger('testScriptExecutor');
logger.info(conf);

logger.info('test is starting');

var sendEmail = require('./../src/services/SendEmail');

sendEmail.sendEmail( conf.mailSettings ,path.resolve(__dirname,'mail_sender_demo'));