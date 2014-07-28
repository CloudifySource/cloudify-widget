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
        .otherwise({
            redirectTo : '/widgets/index'
        });
});