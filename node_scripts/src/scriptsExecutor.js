'use strict';
//include///
var fs = require('fs');
var path = require('path');

// guy - writing in decimal since octal is frowned upon and does not lint
var OCTAL_0644 = 420;

var spawn = require('child_process').spawn;
var args = [];
 
//console.log( 'start' );

var scriptsDir = '..' + path.sep + '_scripts' + path.sep;
var newDir = scriptsDir + 'new' + path.sep;
var executingDir = scriptsDir + 'executing' + path.sep;
var serverNodeIdDir = null;


function createWriteStream( fileName, flag, mode ){
    return fs.createWriteStream( fileName, {
        flags    : 'a',
        encoding : 'utf8',
        mode     : mode
    });
}

function createNewExecutingDir( ){

    if( !fs.existsSync( executingDir ) ){
        console.log( 'executing directory does not exist, will be created' );
        fs.mkdirSync( executingDir );
    }
    if( !fs.existsSync( serverNodeIdDir ) ){
        console.log( '[' + serverNodeIdDir + '] directory does not exist, will be created' );
        fs.mkdirSync( serverNodeIdDir );
    }
}


function writeStatusJsonFile( serverNodeIdDir, jsonFileName, error, exitCode ){

    /*
     Example: Write json status file
     */
    var statusData;
    if( error === null ){
        statusData = {
            exitStatus:exitCode
        };
    }
    else{
        statusData = {
            exitStatus:exitCode,
            exception:error
        };
    }

    var extensionIndex = jsonFileName.indexOf('.json');
    var fileNameWithoutExtension;
    if( extensionIndex > 0 ){
        fileNameWithoutExtension = jsonFileName.substring( 0, extensionIndex );
        //console.log( '>>> fileNameWithouExtension1=' + fileNameWithoutExtension );
    }
    else{
        fileNameWithoutExtension = jsonFileName;
        //console.log( '>>> fileNameWithouExtension2=' + fileNameWithoutExtension );
    }

    fs.writeFile( serverNodeIdDir + fileNameWithoutExtension + '_status.json', JSON.stringify(statusData, null, 4), function(err) {
        if(err) {
            console.log(err);
        }
        else {
            console.log('JSON saved to file');
        }
    });
}


function executeCommand( firstFile, data, commandArgs ){

    console.log( '~~~executeCommand, JSON:' + data);
    data = JSON.parse(data);

    var executable = data.executable;
    var _arguments = data.arguments;
    var serverNodeId = data.serverNodeId;
    var cloudifyHome = data.cloudifyHome;
    var argumentsArray = _arguments.split(',');

    console.log( '>commandArgs=' + commandArgs );
    console.log( '>executable=' + executable );
    console.log( '>arguments=' + _arguments );
    console.log( '>splitted arguments=' + argumentsArray );
    console.log( '>serverNodeId=' + serverNodeId );
    console.log( '>cloudifyHome=' + cloudifyHome );

    process.env.CLOUDIFY_HOME = cloudifyHome;

    var logFile = serverNodeIdDir + 'output-nodeid-' + serverNodeId +'.log';

    console.log( '~~~~~~~~~~~ created logFile=' + logFile );

    var fileLogStream1 = createWriteStream( logFile, 'a', OCTAL_0644 );
    var fileLogStream2 = createWriteStream( logFile, 'a', OCTAL_0644 );

    var concatArray = commandArgs.concat( argumentsArray );
    console.log( 'concatArray=' + concatArray );
    var myCmd = spawn( 	executable, concatArray );

    myCmd.stdout.on('data', function (stdoutData) {
        //console.log( '~~~~~~~myCmd.pid=' +  myCmd.pid );
        console.log('~~~~~~stdoutData   [' + myCmd.pid + ']     ' + stdoutData);
        fileLogStream1.write(stdoutData);
    });

    myCmd.stderr.on('data', function (stderrData) {
        console.log('~~~~stderrData:' + stderrData);
        fileLogStream2.write(stderrData);
    });

    myCmd.on('error', function (err) {
        console.log('!!!!!!!! error thrown:', err);
        writeStatusJsonFile( serverNodeIdDir, firstFile, err, 1 );
    });

    myCmd.on('close', function (code) {
        console.log('!!!!!!!!!!!! process exited with code ' + code );
        writeStatusJsonFile( serverNodeIdDir, firstFile, null, code );
    });
}


//console.log( 'newDir exists:' + fs.existsSync( newDir ) );

if( !fs.existsSync( newDir ) ){
	console.log( '[' + newDir + '] directory does not exist');
	return;
}

var files = fs.readdirSync( newDir );

var filesCount = files.length;
console.log(  'files count:' + filesCount );

if( filesCount  > 0 ){
	console.log( 'All files:' + files );
	var containsStop = files.indexOf( 'stop' ) >= 0;
	console.log( 'Contains stop:' + containsStop );
	if( !containsStop ){
	
		//handle first found folder, move it first to executing folder
		var firstFile = files[ 0 ];
		var serverNodeId = firstFile.substring( 0, firstFile.indexOf('_') );
	    serverNodeIdDir = executingDir + serverNodeId + path.sep;
		
		createNewExecutingDir( serverNodeIdDir );
		
		console.log( 'Before moving file, serverNodeIdDir=' + serverNodeIdDir );

		fs.rename( newDir + firstFile, serverNodeIdDir + firstFile, function (err, data) {
		  if(err) {
			console.log('Rename Error: ' + err);
			return;
		  }

		  console.log( 'After moving [' + firstFile + '] file. data is : ' , data );

		  console.log( 'Before read from JSON, file:' + executingDir + firstFile );
		  fs.readFile( serverNodeIdDir + firstFile, 'utf8', function (err, data) {
			  if (err) {
				  console.log('Read ' + firstFile + ' error: ' + err);
				  return;
			  }

			  executeCommand( firstFile, data, args );
		  });			
		});		
	}
}

console.log( 'end' );






