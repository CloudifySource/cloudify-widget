angular.module('DemoApp', []);


angular.module('DemoApp').controller('DemoCtrl', function($scope, $log){

    $log.info('hello from controlller');
    $scope.appliedProperties = null;
    $scope.properties = [];




    $scope.applyProperties = function(){
        $log.info('applying properties');
        $scope.appliedProperties = $scope.properties;

        frames[0].postMessage( {'name' : 'widget_recipe_properties' , 'data' : [ { 'key' : 'someKey' , 'value' : 'someValue' } , { 'key' : 'property2Key', 'value' : 'property2Value'} ] } , frames[0].location.origin);

        $scope.properties = [];
    };

    $scope.addProperty = function(){
        $log.info('adding property');
        $scope.properties.push({ "key" : null, "value" : null});
    };

    $scope.needToApplyProperties = function(){
        return $scope.properties.length > 0;
    }

});
