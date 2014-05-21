
var path = require('path');
var fs = require('fs-extra');
var async = require('async');
var logger = require('log4js').getLogger('main');
var conf = require('./services/conf');
var services = require('./services');

logger.setLevel(conf.log.level);
logger.trace('conf',JSON.stringify(conf));



exports.initialize = function(){
    for ( var i in conf.directories ){
        var absPath = path.resolve(conf.directories[i]);
        logger.trace('making sure all directories exist',i, absPath );
        fs.mkdirsSync(absPath);
    }
};


function modifyMandrillData( opts, values ){
    var data = opts.mandrill.data;
    function getData( name  ){
        for ( var i = 0; i < data.length; i++ ){
            if ( data[i].name === name ){
                return data[i];
            }
        }

    }


    var linkData = getData( 'link' );
    var linkTitleData = getData( 'linkTitle' );

    linkTitleData.content = linkTitleData.content.replace('$HOST', values.serviceIp );
    linkData.content = '<a href="'+linkData.content.replace('$HOST', values.serviceIp ) + '">' + linkTitleData.content + '</a>';


}


exports.doMain = function(){
    logger.trace('running main');
    var outputWriter = null;
    var opts  = null;
    async.waterfall([

        function( callback ) { exports.initialize(); callback(); },
        function( callback ){
            logger.trace('getting next task');
            services.taskReader.getNextTask( conf.directories.newDirectory , callback )
        },
        function createExecutionDir( _opts, callback ){
            opts = _opts;
            logger.info('creating execution dir');
            outputWriter = new services.taskOutputWriter.Writer( conf.directories.executingDirectory, opts.serverNodeId, opts.action );
            outputWriter.createDir();
            callback();
        },
        function writeConfObj(  callback  ){
            logger.info('writing configuration file');
            outputWriter.writeConfigFile( opts );
            callback(  );
        },
        function executeCommand(  callback  ){
            logger.info('executing command');

            var logAppender = outputWriter.getLogAppender( );
            var handlers = {
                'onStdout' : function( data ){
                    process.stdout.write(data);
                    logAppender.write(data);
                },
                'onStderr' : function( data ){
                    process.stdout.write(data);
                     logAppender.write(data);
                },
                'onClose' : function( exitCode ){
                    if ( exitCode >= 0) {
                        logger.info('exited with code ', exitCode, arguments);
                        outputWriter.writeStatus({
                            'exitCode': exitCode || 1

                        });
                    }

//                    if ( exitCode === 0 ){
                        callback();
//                    }
                },
                'onErr' : function( err ){
                    logger.error(err);
                    outputWriter.writeStatus(
                        {
                            'exitCode' : 1,
                            'error' :  err

                    });


                }
            };
            services.commandExecutor.execute( opts, handlers  );
        },
        /**
         * opts = {
         *      'sendEmail' : true,
         *      'applicationName' : '__recipe_application_name',
         *      'managerIp' : '__manager_ip',
         *      'serviceName' : '__recipe_service_name',
         *      'mandrill' : {
         *          'to' : [ { 'email': , 'name' : , 'type' : }]
         *
         *          'apiKey' : '__apiKey',
         *         'templateName' : '__templateName',
         *         'data' : {
         *             'name' : '__user_name',
         *             'link' : '__service_url e.g. http://$HOST:8099 - directly from the widget',
         *             'linkTitle' : '__email_link_title'
         *         }
         *      }
         * }
         *
         *
         * The method does the following assuming 'sendEmail' is true and mandrill details are available
         *
         *   + resolve service IP
         *   + replace placeholder $HOST on link
         *   + replace data.link value to `<a href='__link__'>__linkTitle__</a>
         *
         *
         * In mandrill, users should define an email with the following placeholder (example) :
         *
         *
         *
         * hello <span mc:edit="name"> __name__ </span>,
         *
         * You service is available at <span mc:edit="link"> __link_html__ </span>
         *
         * Yours,
         * Team
         *
         *
         *
         * NOTE: Mandrill does not provide placeholders for TEXT format emails (support pending) so
         * the text format email should be something like "please revisit widget page __hardcoded_url__" to check the status
         *
         *
         *
         */
        function postExecution( callback ){


            function sendEmail( error, serviceIp ){

                if ( !!error ){
                    logger.error('error while resolving ip',error);
                    throw error;
                }
                logger.info('ip resolved successfully', serviceIp);
                // replace placeholder on link
                modifyMandrillData( opts, { 'serviceIp' : serviceIp } );
                debugger;
                services.mandrillMailSender.sendEmail( opts.mandrill , callback )
            }


            logger.info('post execution handler');
            if ( opts.sendEmail === true && opts.actions === 'install'){
                logger.info('resolving ip');
                 services.cloudifyRestClient.getServiceIp( opts.managerIp, opts.applicationName, opts.serviceName, sendEmail );
            }
        },

        function postEmailSent ( err ) {
            if ( !! err ){
                logger.error(err);
                return;
            }

            logger.info('email sent successfully');
        }

    ], function( err ){
        if ( !!err ){
            logger.error('got an error',err);
            throw err;
        }
        logger.info('finished');
    });


};


if ( require.main === module ){
   exports.doMain();
}

