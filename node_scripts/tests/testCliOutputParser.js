var parser = require('../src/services/cliOutputParser');


var fs = require('fs');
var path = require('path');
var logger = require('log4js').getLogger('testCliOutputParser');

var data = fs.readFileSync( path.resolve(__dirname, 'cloudify.output')).toString();
//logger.info(data);


var cloudifyIp = parser.getIpFromBootsrapOutput( data );
logger.info('cloudify ip is:['+cloudifyIp+']');


