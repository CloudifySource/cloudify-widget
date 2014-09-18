'use strict';
// a directive that allows us to preview the icon.
angular.module('WidgetApp').directive('plunkr', function () {
    return {
        restrict: 'CA',
        scope: {
            'link': '@'
        },
        template: '<a class="plunkr-link" href="{{link}}" target="_blank"> <img src="http://plnkr.co/img/plunker.png" style="height:50px"/> Open in Plunkr!</a>',
        link: function (/*scope, $element, attributes*/) {
        }
    };
});