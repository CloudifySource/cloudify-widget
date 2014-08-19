// Generated on 2013-10-15 using generator-angular 0.3.0
'use strict';

var proxySnippet = require('grunt-connect-proxy/lib/utils').proxyRequest;

var mountFolder = function (connect, dir) {
    return connect.static(require('path').resolve(dir));
};


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
        },
        sass: {
            dist: {
                files: {
                    './styles/main.css': './assets/cloudify/style/angular/main.scss',
                    './styles/demos.css': './assets/cloudify/style/demos.scss',
                    './styles/userDemoIndex.css': './assets/cloudify/style/userDemoIndex.scss',
                    './styles/demo.css': './assets/cloudify/style/demo.scss'

                }
            }
        },
        connect: {
            options: {
                port: 3000,
                hostname: 'localhost'
            },
            proxies: [
                { context: '/backend', port: 9000, host: '127.0.0.1' }
            ],
            livereload: {
                options: {
                    middleware: function (connect) {
                        return [

                            require('connect-livereload')(), // <--- here
                            proxySnippet,
                            mountFolder(connect, '../..')
                        ];
                    }
                }
            }
        },
        watch: {
            options: {
                livereload: true
            },
            compass: {
                files: [
                    './assets/cloudify/style/**/*.{scss,sass}'
                ],

                tasks: ['sass']
            },
            views: {
                files: [
                    './views/**/*.html',
                    'scripts/**/*.js'
                ]
            }

        },
        cacheBust: {
            options: {
                ignorePatterns: ['widget-public-folder/style'],
                rename: false,
                encoding: 'utf8',
                algorithm: 'md5',
                length: 16
            },
            assets: {
                files: [
                    {
                        src: ['index.html']
                    }
                ]
            }
        }

    });

    grunt.registerTask('server', function () {
        grunt.task.run([

            'configureProxies',
            'connect:livereload',
            'watch'
        ]);
    });


    grunt.registerTask('build', function () {
        var tasks = [
            'jshint',
            'sass',
            'cacheBust'
        ];
        grunt.task.run(tasks);
    });

    grunt.registerTask('default', [
        'build'
    ]);
};
