'use strict';

angular.module('WidgetApp', ['ngRoute','ngCookies']).config(function($routeProvider){
    $routeProvider
        .when('/widgets/welcome', {
            templateUrl : '/widgets/welcome.html'
        })
        .when('/widgets/create', {
            controller: 'WidgetsEditCtrl',
            templateUrl: 'views/widgets/edit.html',
            reloadOnSearch: false
        })
        .when('/widgets/:widgetId/edit', {
            controller: 'WidgetsEditCtrl',
            templateUrl: 'views/widgets/edit.html',
            reloadOnSearch: false
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
            templateUrl: 'views/users/account.html'
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