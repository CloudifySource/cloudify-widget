'use strict';

angular.module('WidgetApp', ['ngRoute','ngCookies']).config(function($routeProvider){
    $routeProvider
        .when('/widgets/welcome', {
            templateUrl : '/widgets/welcome.html'
        })
        .when('/widgets/create', {
            controller: 'WidgetsEditCtrl',
            templateUrl: 'views/widgets/edit.html'
        })
        .when('/widgets/:widgetId/edit', {
            controller: 'WidgetsEditCtrl',
            templateUrl: 'views/widgets/edit.html'
        })
        .when('/widgets/index', {
            controller: 'WidgetsIndexCtrl',
            templateUrl: 'views/widgets/index.html'
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
        .when('/public/demo', {
            controller: 'PublicDemoCtrl',
            templateUrl : 'views/public/catalog.html'
        })
        .otherwise({
            redirectTo : '/users/login'
        });
});