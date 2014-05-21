
var fs = require('fs-extra');
var path = require('path');
var logger = require('log4js').getLogger('taskOutputWriter');

function createNewExecutingDir( ){

    if( !fs.existsSync( executingDir ) ){
        console.log( 'executing directory does not exist, will be created' );
        fs.mkdirSync( executingDir );
    }

}


function OutputWriter( baseDir, name, action ){

    var dirName = path.join(baseDir,name);
    var logFile = path.resolve(path.join(dirName, action + '.log'));
    var outputFile = path.resolve(path.join(dirName, 'output.log'));
    var configurationFile = path.join(dirName, action + '.json');
    var statusFile = path.join(dirName, action + '.status');
    logger.info('log file is', logFile );

    this.createDir = function(){

        logger.info('creating dir', dirName);
        fs.mkdirsSync(dirName);

    };
    this.getLogAppender = function(){

        // writing to 2 files. the action.log file and a general output.log file where the server can read output from.

        fs.writeFileSync( logFile, '', 'utf8');

        if ( !fs.existsSync( outputFile )){
            fs.writeFileSync( outputFile, '', 'utf8');
        }

        return {
            'write' : function( data ){
                if ( !typeof(data) === 'string'){
                    data = data.toString();
                }
                fs.appendFileSync( logFile , data , 'utf8');
                fs.appendFileSync( outputFile , data , 'utf8');
            }
        }

    };
    this.writeConfigFile = function( confObj ){
        if ( !confObj ){
            throw 'missing config object to write';
        }

        confObj.processCwd=process.cwd();

        fs.writeFile(configurationFile, JSON.stringify(confObj), function(err) {
            if(err) {
                throw err;
            } else {
                logger.info('configuration file [', configurationFile, '] written successfully' );
            }
        });
    };

    this.writeStatus = function( status ){
        if ( !status ){
            throw 'missing status file'
        }

        fs.writeFile( statusFile, JSON.stringify(status), function(err){
            if ( !!err){
                throw err;
            }else{
                logger.info('status file [', statusFile, '] written successfully');
            }
        });
    }
}

exports.Writer = OutputWriter;
