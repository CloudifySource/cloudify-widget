var logger = require('log4js').getLogger('commandExecutor');
var path = require('path');
var spawn = require('child_process').spawn;


/**
 *
 *
 * opts - {
 *     "arguments" - CSV of arguments to run the command with "bootstrap-cloud,softlayer"
 *     "executable" - cloudify.sh location
 *     "cloudifyHome" - the cloudify home directory
 * }
 *
 *
 * handlers: {
 *
 *      "onErr" - on error `function(error){}`
 *      "onStdout" - on std out data  `function(data){}`
 *      "onStderr" - on err data  `function(data){}`
  *     "onClose" - on close `function(exitCode){}`
 *
 * }
 *
 *
 *
 * @param opts
 */

exports.execute = function( opts , handlers ){

    logger.info('executing', opts);

    var executable = opts.executable;
    var argumentsArray = opts.arguments.split(",");

    var cloudifyHome = opts.cloudifyHome;


    logger.info( 'executable=' , path.resolve(executable) );
    logger.info( 'splitted arguments=' , argumentsArray );
    logger.info( 'cloudifyHome=' , cloudifyHome );

    process.env.CLOUDIFY_HOME = cloudifyHome;

    var myCmd = spawn( 	executable, argumentsArray );

    if ( !handlers.onStdout ){
        throw 'stdout handler is missing!';
    }
    myCmd.stdout.on('data', handlers.onStdout );

    if ( !handlers.onStderr ){
        throw 'stderr handler is missing'
    }
    myCmd.stderr.on('data', handlers.onStderr );

    if ( !handlers.onErr ){
        throw 'onerr handler is missing';
    }
    myCmd.on('error', handlers.onErr );

    if ( !handlers.onClose ){
        throw 'onclose handler is missing';
    }
    myCmd.on('exit', handlers.onClose);
};