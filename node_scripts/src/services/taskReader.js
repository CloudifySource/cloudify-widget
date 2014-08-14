'use strict';

/**
 *
 *
 * Handles the files' circle of life.
 *
 * We use the file system to propagate tasks.
 *
 * New tasks are in folder X. When they are first encountered, an output directory is created and the file is moved to
 * the output directory so that the next worker in line won't run into it.
 *
 * @param newDir - directory for new tasks
 *
 **/


var logger = require('log4js').getLogger('taskReader');
var fs = require('fs');


/**
 * reads the first file in the folder and deletes it.
 * @param newDir
 * @param callback
 */
exports.getNextTask = function( newDir, callback ) {

    callback = callback || function () {
    }; // default noop;

    if (!fs.existsSync(newDir)) {
        console.log('[' + newDir + '] directory does not exist');
        callback(new Error('directory [' + newDir + '] does not exist'), null);
    }

    var files = fs.readdirSync(newDir);



    var filesCount = files.length;


    if (filesCount > 0) {
        logger.info('All files:' + files);

        // if contains file 'stop' do nothing.
        var containsStop = files.indexOf('stop') >= 0;
        logger.info('Contains stop:' + containsStop);
        if (!!containsStop) {
            return;
        }

        //handle first found folder, move it first to executing folder
        var firstFile = newDir + '/' + files[ 0 ];
        logger.info('handling file', firstFile);
        fs.readFile(firstFile, function (err, data) {
            data = data.toString();
            if ( !!err ){
                callback(err);
                return;
            }

            fs.unlink(firstFile, function( err ){
                if ( !!err ){
                    callback(err);
                    return;
                }
                callback(null, JSON.parse(data));
            });

        });
    }
};

