'use strict';

angular.module('WidgetApp').controller('PublicDemoCtrl', function ($scope, $location, $log, WidgetsService, $routeParams, $http, $cookieStore, $timeout) {


    WidgetsService.demo.getDemoWidgets($routeParams.email || 'default_demo@gigaspaces.com').then(function (result) {
        $scope.widgets = result.data;
        try {
            $scope.selectedWidget = $scope.widgets[0];
        } catch (e) {
            $log.error('unable to choose default widget', e);
        }
    });

    $scope.isSelected = function (widget) {
        return $scope.selectedWidget === widget;
    };


    $scope.menuClick = function (widget) {
        $scope.selectedWidget = widget;
    };


    $log.info('hello from controlller');
    $scope.appliedProperties = null;
    $scope.properties = [];


    $scope.applyProperties = function () {
        $log.info('applying properties');
        $scope.appliedProperties = $scope.properties;
        for (var i = 0; i < $scope.appliedProperties.length; i++) {
            var prop = $scope.appliedProperties[i];
            try {
                if (!isNaN(parseInt(prop.value))) {
                    console.log('found a numeric value', prop);
                    prop.value = parseInt(prop.value);
                }
            } catch (e) {
                console.log('error', e);
            }
        }


        frames[0].postMessage({'name': 'widget_recipe_properties', 'data': $scope.appliedProperties }, frames[0].location.origin);

        $scope.properties = [];
    };

    $scope.addProperty = function () {
        $log.info('adding property');
        $scope.properties.push({ "key": null, "value": null});
    };

    $scope.needToApplyProperties = function () {
        return $scope.properties.length > 0;
    }
});