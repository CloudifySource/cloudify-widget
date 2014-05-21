var conf = require('./../src/services/conf');
var logger = require('log4js').getLogger('testMandrillSender');


var mandrillMailSender = require('../src/services/MandrillMailSender');


mandrillMailSender.sendEmail( conf.mailchimp , function( err ){
    if ( !!err ){
        throw err;
    }
    logger.info('test ended successfully');
});