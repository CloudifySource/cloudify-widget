var fs = require('fs');
var path = require('path');
var _  = require('lodash');
var meConf = path.resolve('conf/dev/me.json');
var prodConf = path.resolve(__dirname, '../../conf/prod.json');
var logger = require('log4js').getLogger('conf');

logger.trace('me conf is at ', meConf );
logger.trace('prod conf is at ', prodConf );


var data = fs.readFileSync(prodConf, 'utf8');
if (!!data) {
    _.merge(module.exports, JSON.parse(data));
}
data = fs.readFileSync(meConf, 'utf8');
if (!!data) {
    _.merge(module.exports, JSON.parse(data));
}