angular.module('DemoApp', []);


angular.module('DemoApp').controller('DemoCtrl', function($scope, $log){

    $log.info('hello from controlller');
    $scope.appliedProperties = null;
    $scope.properties = [];




    $scope.applyProperties = function(){
        $log.info('applying properties');
        $scope.appliedProperties = $scope.properties;
        for ( var i = 0; i < $scope.appliedProperties.length ; i++ ){
            var prop = $scope.appliedProperties[i];
            try {
                if (!isNaN(parseInt(prop.value))){
                    console.log('found a numeric value', prop);
                    prop.value = parseInt(prop.value);
                }
            }catch(e){
                console.log('error',e);
            }
        }


        frames[0].postMessage( {'name' : 'widget_recipe_properties' , 'data' : $scope.appliedProperties } , frames[0].location.origin);

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
