'use strict';

angular.module('WidgetApp').controller('PublicDemoCtrl', function ($scope, $location, $log, WidgetsService, $routeParams) {


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

    $scope.properties = [];
    $scope.propertiesMap = {};
    var propertiesChanged = false;


    function propertiesByFormat( format ){
        if ( format === 'list'){
            return $scope.properties;
        }else{
            return $scope.propertiesMap;

        }
    }

    function parseIntProperties(){

        _.each($scope.properties, function(prop){
            try {

                if (!isNaN(prop.value) && !isNaN(parseInt(prop.value, 10))) {
                    console.log('found a numeric value', prop);
                    prop.value = parseInt(prop.value, 10);
                }
            } catch (e) {
                console.log('error', e);
            }
        });
    }

    $scope.$watch('properties', function(){
        $log.info('converting ints for the demo');
        parseIntProperties();
        $log.info('creating a map clone for the demo');
        $scope.propertiesMap = {};//reset
        try {
            _.each($scope.properties, function(item) { $scope.propertiesMap[item.key] = item.value; });
        } catch (e) {
            $log.error('error converting to propertiesMap');
        }
        propertiesChanged = true;
    },true);

    $scope.removeProperty = function(i){
        _.remove($scope.properties, function(e){ return e === i; });
    };

    $scope.applyProperties = function ( format ) {
        $log.info('applying properties');
        frames[0].postMessage({'name': 'widget_recipe_properties', 'data': propertiesByFormat(format)}, frames[0].location.origin);
        propertiesChanged = false;
    };

    $scope.addProperty = function () {
        $log.info('adding property');
        $scope.properties.push({ 'key': null, 'value': null});
    };

    $scope.needToApplyProperties = function () {
        return !!propertiesChanged;
    };

    setTimeout(function(){propertiesChanged = false;},0);
});