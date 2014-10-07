'use strict';
// a directive that allows us to preview the icon.
angular.module('WidgetApp').directive('documentationItem', function () {
    return {
        restrict: 'CA',
        scope: {
            'item': '='
        },
        templateUrl : 'views/directives/_documentationItem.html',
        link: function (/*scope, $element/*, attributes*/) {
        }
    };
});