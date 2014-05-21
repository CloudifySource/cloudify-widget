var fs = require('fs');
var path = require('path');
var logger = require('log4js').getLogger('testFileStream');


var file = path.resolve('guy.txt');
logger.info('writing to file',file);


fs.appendFile("guy.txt", "Hey there!1\n");
fs.appendFile("guy.txt", "Hey there!2\n");
fs.appendFile("guy.txt", "Hey there!3\n");
fs.appendFile("guy.txt", "Hey there!4\n");
//fs.appendFile("guy.txt", "Hey there!5");
//createWriteStream( 'guy.txt', 'a', '0644').write('hello world!\n');
//createWriteStream( 'guy.txt', 'a', '0644').write('hello world!2\n');
//createWriteStream( 'guy.txt', 'a', '0644').write('hello world!3\n');
//createWriteStream( 'guy.txt', 'a', '0644').write('hello world!4\n');