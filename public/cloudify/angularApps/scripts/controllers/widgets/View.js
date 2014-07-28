'use strict';

angular.module('WidgetApp').controller('WidgetsViewCtrl', function($scope, WidgetsService, $controller, $log, $location, $routeParams, WidgetThemes  ){


    $scope.includePath = 'views/widgets/themes/loading.html';

    if ( !!$routeParams.widgetKey ){
        WidgetsService.getWidgetByKey( $routeParams.widgetKey).then(function(result){
            $scope.widget = result.data;

            $scope.widget.data = JSON.parse(result.data.data);
            $scope.includePath = 'views/widgets/themes/' + ( $scope.widget.data.theme || WidgetThemes.getDefault().id) + '.html';
            $log.info('includePath is', $scope.includePath);
            $controller('WidgetCtrl', {$scope: $scope}); //This works
        });
    }

});