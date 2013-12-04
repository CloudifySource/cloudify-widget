//include///
var fs = require("fs");
var path = require("path");
var exec = require('child_process').exec;
var spawn = require('child_process').spawn;
var args = [];
 
console.log( 'start' );

var scriptsDir = '..\\_scripts\\';
var newDir = scriptsDir + 'new' + path.sep;
var executingDir = scriptsDir + 'executing' + path.sep;
var executedDir = scriptsDir + 'executed' + path.sep;

console.log( "newDir exists:" + fs.existsSync( newDir ) );

if( !fs.existsSync( newDir ) ){
	console.log( '[' + newDir + '] directory does not exist');
	return;
}

var files = fs.readdirSync( newDir );
console.log( 'All files:' + files );

var filesCount = files.length;
console.log(  "files count:" + filesCount );

if( filesCount  > 0 ){
	var containsStop = files.indexOf( 'stop' ) >= 0;
	console.log( 'Contains stop:' + containsStop );
	if( !containsStop ){
	
		//handle first found folder, move it first to executing folder
		var firstFile = files[ 0 ];
		var serverNodeId = firstFile.substring( 0, firstFile.indexOf('_') );
		var serverNodeIdDir = executingDir + serverNodeId + path.sep;
		
		createNewExecutingDir( serverNodeIdDir );
		
		console.log( 'Before moving file, serverNodeIdDir=' + serverNodeIdDir );

		fs.rename( newDir + firstFile, serverNodeIdDir + firstFile, function (err, data) {
		  if(err) {
			console.log('Rename Error: ' + err);
			return;
		  }
			console.log( 'After moving file' );
		});		
		
/*
		var isBootstrapOperation = firstFile.indexOf("_bootstrap") > 0;
		var isInstallOperation = firstFile.indexOf("_install") > 0;
		console.log(  'firstFile:' + firstFile + ',isBootstrapOperation=' + isBootstrapOperation + ',isInstallOperation=' + isInstallOperation + ',serverNodeId=' + serverNodeId + '\n');
*/		
		console.log( 'Before read fom JSON, file:' + executingDir + firstFile );
		fs.readFile( serverNodeIdDir + firstFile, 'utf8', function (err, data) {
			if (err) {
				console.log('Read ' + firstFile + ' error: ' + err);
				return;
			}
			
			executeCommand( firstFile, data, args );
			
			/*
			if( isBootstrapOperation ){
				performBootstrap( data );
			}
			else if( isInstallOperation ){
				performInstall( data );
			}*/
		});
	
		//writeStatusJsonFile( serverNodeIdDir, firstFile );
	}
}

console.log( 'end' );

function createNewExecutingDir( serverNodeId ){
	
	if( !fs.existsSync( executingDir ) ){		
		console.log( 'executing directory does not exist, will be created' );
		fs.mkdirSync( executingDir );
	}		
	if( !fs.existsSync( serverNodeIdDir ) ){		
		console.log( '[' + serverNodeIdDir + '] directory does not exist, will be created' );
		fs.mkdirSync( serverNodeIdDir );
	}	
}

function executeCommand( firstFile, data, commandArgs ){

	console.log( '~~~executeCommand, JSON:' + data);
	data = JSON.parse(data);
	
	var cmdLine = data.cmdLine;//'D:\\gigaspaces-xap-premium-9.7.0-m7-b10491-236\\bin\\gs-ui.bat';//data.cmdLine;
	var args = data.args;
	var advancedparams = data.advancedparams;
	var serverNodeId = data.serverNodeId;
	var cloudifyHome = data.cloudifyHome;
	var handlePrivateKey = data.handlePrivateKey;
	
	console.log( '>cmdLine=' + cmdLine );
	console.log( '>advancedparams=' + advancedparams );
	console.log( '>serverNodeId=' + serverNodeId );
	console.log( '>cloudifyHome=' + cloudifyHome );
	console.log( '>handlePrivateKey=' + handlePrivateKey );	

	process.env['CLOUDIFY_HOME'] = cloudifyHome;
	
	var logFile = serverNodeIdDir + 'output-nodeid-' + serverNodeId +'.log';
	
	console.log( '~~~~~~~~~~~ created logFile=' + logFile );
	
	var fileLogStream1 = createWriteStream( logFile, 'a', 0644 );
	var fileLogStream2 = createWriteStream( logFile, 'a', 0644 );
	
	myCmd = spawn( cmdLine );

	myCmd.stdout.on("data", function (stdoutData) {
		console.log( '~~~~~~~myCmd.pid=' +  myCmd.pid );
		console.log("~~~~~~stdoutData" + stdoutData); 
		fileLogStream1.write(stdoutData);
		/*
		//check for exit indication
		if( stdoutData.indexOf('Good Bye!') >= 0 ) {
			writeStatusJsonFile( serverNodeIdDir, firstFile, null );
			fileLogStream1.
			fileLogStream2.
		}*/
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

function createWriteStream( fileName, flag, mode ){
	return fs.createWriteStream( fileName, {
		flags    : flag,
		encoding : 'utf8',
		mode     : mode
	});
}

function writeStatusJsonFile( serverNodeIdDir, jsonFileName, error, exitCode ){

		/*
			Example: Write json status file
		*/
		var statusData
		if( error == null ){
			statusData = {
				exitStatus:exitCode
			}	
		}
		else{
			statusData = {
				exitStatus:exitCode,
				exception:error
			}		
		}
		
		var extensionIndex = jsonFileName.indexOf(".json");
		var fileNameWithoutExtension;
		if( extensionIndex > 0 ){
			fileNameWithoutExtension = jsonFileName.substring( 0, extensionIndex );
			console.log( '>>> fileNameWithouExtension1=' + fileNameWithoutExtension );
		}
		else{
			fileNameWithoutExtension = jsonFileName;
			console.log( '>>> fileNameWithouExtension2=' + fileNameWithoutExtension );
		}
		
		fs.writeFile( serverNodeIdDir + fileNameWithoutExtension + "_status.json", JSON.stringify(statusData, null, 4), function(err) {
			if(err) {
				console.log(err);
			} 
			else {
				console.log("JSON saved to file");
			}
		}); 
}

/*
function performBootstrap( data ){
	console.log( '~~~performBootstrap, JSON:' + data);
	data = JSON.parse(data);
	
	var cmdLine = data.cmdLine;
	var args = data.args;
	var advancedparams = data.advancedparams;
	var serverNodeId = data.serverNodeId;
	var cloudifyHome = data.cloudifyHome;
	var handlePrivateKey = data.handlePrivateKey;
	
	console.log( '>cmdLine=' + cmdLine );
	console.log( '>advancedparams=' + advancedparams );
	console.log( '>serverNodeId=' + serverNodeId );
	console.log( '>cloudifyHome=' + cloudifyHome );
	console.log( '>handlePrivateKey=' + handlePrivateKey );
	
	exec( cmdLine, function (error, stdout, stderr) {
		console.log('performBootstrap, stdout: ' + stdout);
		console.log('performBootstrap, stderr: ' + stderr);
		if( error !== null ) {
			console.log('performBootstrap,exec error: ' + error);
		}
	});
}

function performInstall( data ){
	console.log( '~~~performInstall, JSON:' + data);
	data = JSON.parse(data);

	var cmdLine = data.cmdLine;
	var advancedparams = data.advancedparams;
	var serverNodeId = data.serverNodeId;
	var cloudifyHome = data.cloudifyHome;
	
	console.log( '>cmdLine=' + cmdLine );
	console.log( '>advancedparams=' + advancedparams );
	console.log( '>serverNodeId=' + serverNodeId );
	console.log( '>cloudifyHome=' + cloudifyHome );	
	
	exec( cmdLine, function (error, stdout, stderr) {
		console.log('performInstall, stdout: ' + stdout);
		console.log('performInstall, stderr: ' + stderr);
		if( error !== null ) {
			console.log('performInstall, exec error: ' + error);
		}
	});
}*/