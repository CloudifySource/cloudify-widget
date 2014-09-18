'use strict';

angular.module('WidgetApp', ['ngRoute','ngCookies', 'hljs']).config(function($routeProvider){
    $routeProvider
        .when('/widgets/welcome', {
            templateUrl : '/widgets/welcome.html'
        })
        .when('/widgets/create', {
            controller: 'WidgetsEditCtrl',
            templateUrl: 'views/widgets/edit.html',
            reloadOnSearch: false
        })
        .when('/configuration/docs', {
            templateUrl: 'views/configuration/docs.html'
        })
        .when('/widgets/:widgetId/edit', {
            controller: 'WidgetsEditCtrl',
            templateUrl: 'views/widgets/edit.html',
            reloadOnSearch: false
        })
        .when('/pool/output', {
            controller: 'PoolsCreateMachineOutputCtrl',
            templateUrl : 'views/pool/createMachineOutput.html',
            resolve : {
                section : function(){ return 'poolOutput'; }
            }
        })
        .when('/widgets/index', {
            controller: 'WidgetsIndexCtrl',
            templateUrl: 'views/widgets/index.html',
            resolve: {
                section : function() { return 'widgets'; }
            }
        })
        .when('/user/account', {
            controller: 'UsersAccountCtrl',
            templateUrl: 'views/users/account.html',
            resolve: {
                section: function(){ return 'account'; }
            }
        })
        .when('/widgets/:widgetId/preview', {
            controller: 'WidgetsPreviewCtrl',
            templateUrl: 'views/widgets/preview.html'
        })
        .when('/widgets/:widgetKey/view', {
            controller: 'WidgetsViewCtrl',
            templateUrl: 'views/widgets/view.html'
        })
        .when('/users/login', {
            controller: 'UsersLoginCtrl',
            templateUrl: 'views/users/login.html'
        })
        .when('/logins/custom', {
            controller : 'LoginsCustomCtrl',
            templateUrl : 'views/logins/custom.html'
        })
        .when('/logins/google/callback', {
            controller : 'LoginsGoogleCallbackCtrl',
            templateUrl :'views/logins/googleCallback.html'
        })
        .when('/configuration', {
            templateUrl : 'views/configuration/index.html',
            resolve: {
                section : function() { return 'configuration'; }
            }
        })
        .when('/documentation/configuration', {
            templateUrl : 'views/documentation/index.html',
            controller: 'DocsIndexCtrl',
            reloadOnSearch: false,
            resolve : {
                section : function(){ return 'documentation'; }
            }

        })
        .when('/pool', {
            templateUrl : 'views/pool/index.html',
            controller : 'PoolsIndexCtrl',
            resolve: {
                section : function() { return 'pool'; }
            }
        })
        .when('/public/demo', {
            controller: 'PublicDemoCtrl',
            templateUrl : 'views/public/catalog.html'
        })
        .otherwise({
            redirectTo : '/public/demo'
        });
});