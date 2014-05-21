



var path = require('path');
var utils = require('./utils');
var conf = require('./../src/services/conf');

var logger = require('log4js').getLogger('taskReader');

var taskReader = require('../src/services/taskReader');

//logger.info(conf);

logger.info('test is starting');

var newDirPath = path.resolve(conf.taskReader.newDir);
utils.writeData();

function getNextTask(){
    taskReader.getNextTask( newDirPath , function(){
//        logger.info('got next task');
//        logger.info(arguments);
    });
}

setInterval(getNextTask, 1000);


