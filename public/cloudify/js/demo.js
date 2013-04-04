'use strict';
var widgetConfig = function($routeProvider){ $routeProvider.when( '/', { controller: 'DemoController', templateUrl: 'widgetTemplate' } ) };
var WidgetApp = angular.module( 'DemoApp', ['ngCookies'] ).config( widgetConfig );
WidgetApp.controller('DemoController', function($scope, $location, $routeParams, $http, $cookieStore ){
    $scope["widgets"] = $http.get(jsRoutes.controllers.DemosController.listWidgetForDemoUser( $scope["userId"] ).url ).then(function(data){
        var searchWidgetId = $cookieStore.get("widgetId");
        var cachedWidget = $( data.data ).filter(function(index,value){ return value.id == searchWidgetId })[0];
        var selectedWidget = angular.isDefined(cachedWidget) ? cachedWidget : data.data[0];
        $scope.menuClick(selectedWidget);
        return data.data;
    } );
    $scope.menuClick = function( widget ){
        $scope.selectedWidget = widget;
        $cookieStore.put("widgetId",widget.id);
    };
});