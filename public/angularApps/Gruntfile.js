// Generated on 2013-10-15 using generator-angular 0.3.0
'use strict';

module.exports = function (grunt) {


    // load all grunt tasks
    require('matchdep').filterDev('grunt-*').forEach(grunt.loadNpmTasks);


    grunt.initConfig({
        jshint: {
            options: {
                jshintrc: '.jshintrc'
            },
            all: [
                'Gruntfile.js',
                'scripts/**/*.js'
            ]
        } ,
        sass: {
            dist: {
                files: {
                    '../style/angular/main.css': '../../app/assets/cloudify/style/angular/main.scss',
                    '../style/demos.css': '../../app/assets/cloudify/style/demos.scss',
                    '../style/userDemoIndex.css': '../../app/assets/cloudify/style/userDemoIndex.scss',
                    '../style/demo.css': '../../app/assets/cloudify/style/demo.scss'

                }
            }
        },
        cacheBust: {
            options: {
                ignorePatterns: ['public/style'],
                rename:false,
                encoding: 'utf8',
                algorithm: 'md5',
                length: 16
            },
            assets: {
                files: [{
                    src: ['index.html']
                }]
            }
        }

    });


    grunt.registerTask('build', function () {
        var tasks = [
            'jshint',
            'sass'
        ];
        grunt.task.run(tasks);
    });

    grunt.registerTask('default', [
        'build'
    ]);
};
