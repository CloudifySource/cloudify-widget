
var path = require('path');
var fs = require('fs-extra');
var logger = require('log4js').getLogger('main');
var conf = require('./services/conf');

logger.info('conf',JSON.stringify(conf));


exports.initialize = function(){
    for ( var i in conf.directories ){
        var absPath = path.resolve(conf.directories[i]);
        logger.info('making sure all directories exist',i, absPath );
        fs.mkdirsSync(absPath);
    }
};


if ( require.main === module ){
    logger.info('running main');
    exports.initialize();
}

