'use strict';
angular.module('WidgetApp').directive('img', function(){
    return {
        restrict:'E',
        link:function(scope,element){
            element.error(function(){
                $(this).hide();
            });
            element.load(function(){
                $(this).show();
            });
        }
    };
});
