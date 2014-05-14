var fs = require('fs');
function createWriteStream( fileName, flag, mode ){
    return fs.createWriteStream( fileName, {
        flags    : 'a',
        encoding : 'utf8',
        mode     : mode
    });
}


createWriteStream( 'guy.txt', 'a', '0644').write('hello world!\n');
createWriteStream( 'guy.txt', 'a', '0644').write('hello world!2\n');
createWriteStream( 'guy.txt', 'a', '0644').write('hello world!3\n');
createWriteStream( 'guy.txt', 'a', '0644').write('hello world!4\n');