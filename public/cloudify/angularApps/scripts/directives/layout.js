'use strict';

/** This is a holy-grail implementation **/
/** read more at :
 /** other reference implementations just in case :
 http://fiddle.jshell.net/teresko/EkTVv/show/
 http://alistapart.com/article/holygrail

 Use it wisely
 **/
angular.module('WidgetApp')
    .directive('layout', function () {
        return {
            templateUrl: 'views/directives/layout.html',
            restrict: 'C',
            transclude: true,
            replace: true,
            link: function postLink(/*scope, element*//*, attrs*/) {
//                function doIt(){
//            }
            }
        };
    });
