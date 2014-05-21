var util = require('util');


var Client = require('node-rest-client').Client;

var logger = require('log4js').getLogger('cloudifyRestClient');

client = new Client();

// direct way


/**
 *
 * @param ip - the cloudify management ip
 * @param application - the application's name e.g. "default"
 * @param service - recipe's service name (this is the service we would like to get ip for).
 * @param callback - func( err, managementIp:string )
 */
exports.getServiceIp = function( ip, application, service , callback ){
    var getPublicIp =  util.format('http://%s:8100/admin/ProcessingUnits/Names/%s.%s/Instances/0/ServiceDetailsByServiceId/USM/Attributes/Cloud%20Public%20IP', ip, application, service);
    var getPrivateIp =  util.format('http://%s:8100/admin/ProcessingUnits/Names/%s.%s/Instances/0/ServiceDetailsByServiceId/USM/Attributes/Cloud%20Private%20IP', ip, application, service);
    logger.info('requesting [%s]', getPublicIp);
    client.get(getPublicIp, function(data){ logger.info(typeof(data));
            logger.info(data);
            var ip =JSON.parse(data)["Cloud Public IP"];
            if ( !ip || ip.length === 0 ){
                logger.info('requesting [%s]', getPrivateIp);
                client.get( getPrivateIp, function(data){
                    logger.info(data);
                    debugger;
                    var ip = JSON.parse(data)["Cloud Private IP"];
                    callback(null,ip);
                })
            }else {
                callback(null, ip);
            }
        }
    )
};


