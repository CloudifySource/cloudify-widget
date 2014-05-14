'use strict';
var logger = require('log4js').getLogger('SendEmail');
var _ = require('lodash');

function sendMail(mailSettings, content) {
    var nodemailer = require('nodemailer');

// create reusable transport method (opens pool of SMTP connections)
    var smtpTransport = nodemailer.createTransport(mailSettings.type, mailSettings.options);

// setup e-mail data with unicode symbols
    var mailOptions = { };


    _.assign(mailOptions, mailSettings.emails.logs);
    _.assign(mailOptions, {


        text: content, // plaintext body
        html: content // html body
    });

// send mail with defined transport object
    smtpTransport.sendMail(mailOptions, function (error, response) {
        if (error) {
            console.log(error);
        } else {
            console.log('Message sent: ' + response.message);
        }

        // if you don't want to use this transport object anymore, uncomment following line
        //smtpTransport.close(); // shut down the connection pool, no more messages
    });
}

function readAllFiles(dir, callback) {
    var fs = require('fs');


    var data = {};

    fs.readdir(dir, function (err, files) {
        if (err) {
            throw err;
        }
        var c = 0;
        files.forEach(function (file) {
            if (file.indexOf('.log') < 0) {
                return;
            }
            c++;
            fs.readFile(dir + file, 'utf-8', function (err, html) {
                if (err) {
                    callback(err);
                    throw err;
                }
                data[file] = html;
                if (0 === --c) {
                    callback(null, data);
                    console.log(data);  //socket.emit('init', {data: data});
                }
            });
        });
    });
}


/** reads all files with JSON suffix and sends their content to email address **/
exports.sendEmail = function (mailSettings, directory) {

    if (directory[directory.length - 1] !== '/') {
        directory = directory + '/';
    }
    readAllFiles(directory, function (err, data) {
        logger.info('got all files ', data);

        var mailContent = '';
        for (var i in data) {
            mailContent += '<h1>' + i + '</h1>';
            mailContent += '<pre>' + data[i] + '</pre>';
        }


        sendMail(mailSettings, mailContent);
    });


};


