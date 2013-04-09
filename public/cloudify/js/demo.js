//read more at : http://jsfiddle.net/ganarajpr/LQGE2/
'use strict';
var widgetConfig = function($routeProvider){ $routeProvider.when( '/', { controller: 'DemoController', templateUrl: 'widgetTemplate' } ) };
var WidgetApp = angular.module( 'DemoApp', ['ngCookies'] ).config( widgetConfig );
WidgetApp.controller('DemoController', function($scope, $location, $routeParams, $http, $cookieStore, $timeout ){
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


    /////////////// Walkthrough!
    var walkthroughChecker = null;
    $scope.hideWT = true; // default
    $scope.$showWT = function(){
        $scope.hideWT = false;
        $(".walkthrough" ).fadeIn();
    };

    $scope.$hideWT = function(){
        $scope.hideWT = true;
        $(".walkthrough" ).fadeOut();
    };


    $scope.dismissWalkthrough = function(){
            $cookieStore.put("dismissWT",true);
            $scope.$hideWT();
    };

    $scope.shouldShowWalkthrough = function(){
        var dismissWTCookieValue = $cookieStore.get("dismissWT");
        return  !angular.isDefined(dismissWTCookieValue) || dismissWTCookieValue.toString() != "true";
    };

    $scope.checkWalkthrough = function(){
        if ( !$scope.shouldShowWalkthrough() ){
            $scope.dismissWalkthrough();
            clearInterval(walkthroughChecker);
        }
    };

    if ( $scope.shouldShowWalkthrough() ){
        $timeout(function(){
            $scope.$showWT();
        walkthroughChecker = setInterval( function(){  $scope.checkWalkthrough(); $scope.$apply() }, 1000 );
        }, 2000);
    }else{
        $scope.$hideWT();
    }
});

