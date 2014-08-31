'use strict';
// a directive that allows us to preview the icon.
angular.module('WidgetApp').directive('countdown', function ($timeout) {
    return {
        restrict: 'CA',
        scope: {
            'deadline': '='
        },
        template: '<span></span>',
        link: function (scope, $element/*, attributes*/) {
            function update(){

                $element.text(Math.max(scope.deadline - Date.now(),0));
                $timeout(update,10);
            }
            update();

            scope.$watch('deadline', update);
        }
    };
});