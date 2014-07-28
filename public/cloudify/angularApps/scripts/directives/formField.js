'use strict';

angular.module('WidgetApp')
    .directive('formField', function () {
        return {
            templateUrl: 'views/directives/formField.html',
            restrict: 'A',
            transclude: true,
            replace: true,
            scope:{
                'title' : '@',
                'errorId' : '@',
                'tooltip' : '@',
                'required' : '@',
                'fieldId' : '@',
                'type' : '@',
                'disabled' : '@',
                'value' : '='
            },
            link: function postLink(/*scope, element*//*, attrs*/) {

            }
        };
    });
