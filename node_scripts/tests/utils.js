var path = require('path');
var _ = require('lodash');
var conf = require('./../src/services/conf');
var newDirPath = path.resolve(conf.directories.newDirectory);
var logger = require('log4js').getLogger('utils');




exports.writeData = function(){
    var reqData1 = _.merge({'data1': 'data1', 'serverNodeId': '1'}, conf);
    var reqData2 = _.merge({'data1': 'data2', 'serverNodeId': '2'}, conf);
    var reqData3 = _.merge({'data1': 'data3', 'serverNodeId': '3'}, conf);



    function writeRequest( filename, reqData ){
        var fs = require('fs');
        var outputFileName = newDirPath + '/' + filename;
        logger.info('writing file ', outputFileName );
        fs.writeFileSync(outputFileName , JSON.stringify(reqData) );
    }

    writeRequest( 'req1.json',reqData1 );
    writeRequest( 'req2.json',reqData2 );
    writeRequest( 'req3.json',reqData3 );
};
