//read more at : http://jsfiddle.net/ganarajpr/LQGE2/
'use strict';
var widgetConfig = function($routeProvider){ $routeProvider.when( '/', { controller: 'DemoController', templateUrl: 'widgetTemplate' } ) };
var WidgetApp = angular.module( 'DemoApp', ['ngCookies'] ).config( widgetConfig );
WidgetApp.controller('DemoController', function($scope, $location, $routeParams, $http, $cookieStore, $timeout ){
    $scope["widgets"] = $http.get(jsRoutes.controllers.DemosController.listWidgetForDemoUser( $scope["userId"] ).url ).then(function(data){
        var searchWidgetId = $cookieStore.get("widgetId");
        var cachedWidget = $( data.data ).filter(function(index,value){ return value.id == searchWidgetId })[0];

        // return the cached widget or couchbase.
        var selectedWidget = angular.isDefined(cachedWidget) ? cachedWidget : $.grep(data.data, function(item,index){ return item.productName == "Couchbase"});
        selectedWidget =  selectedWidget.length > 0  ? selectedWidget[0] : null; // remove array from JQuery



        // if no cached widget and no couchbase default to 0.
        $scope.menuClick(selectedWidget == null ? data.data[0] : selectedWidget );
        return data.data;
    } );
    $scope.menuClick = function( widget ){
        $scope.selectedWidget = widget;
        $cookieStore.put("widgetId",widget.id);
    };

    $scope.demoSort = function ( widget ){
        return widget.productName == "Couchbase" ? 0 : 1;
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

