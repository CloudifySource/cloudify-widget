'use strict';

angular.module('WidgetApp').controller('WidgetsPreviewCtrl', function($scope, WidgetsService, $log, $location, $routeParams  ){

    if ( !!$routeParams.widgetId ){
        WidgetsService.getWidget( $routeParams.widgetId).then(function(result){
            $scope.widget = result.data;
        });
    }


    $scope.delete = function(widget){
        if ( !!confirm('are you sure you want to delete ' + widget.productName )){
            WidgetsService.deleteWidget(widget).then(function(){
                $location.path('/widgets/index');
            });
        }
    };


});