'use strict';
// a directive that allows us to preview the icon.
angular.module('WidgetApp').directive('embedCode', function ($window, $compile, $timeout, $log) {
    return {
        restrict: 'CA',
        transclude: true,
        scope: {
            'widget': '=',
            'asCode' : '@'
        },
        replace: false,
//    <iframe src="http://localhost:9000/widget/291edd87-c17e-422c-9bdc-f5686b23a6ed/display" scrolling="no" width="600px" height="463px" frameborder="no"></iframe>
        template: '<iframe src="{{widgetUrl}}"  scrolling="no" width="600px" height="463px" frameborder="no"></iframe>',
//        replace:true,
        link: function (scope, $element/*, attributes*/) {
            var $scope = scope;
            var contents = $element.html();

            scope.$watch('widget', function (newValue) {
                if (!!newValue) {
//                     <iframe src="http://localhost:9000/widget/widget?apiKey=291edd87-c17e-422c-9bdc-f5686b23a6ed&amp;showAdvanced=false&amp;title=BluStratus%201T&amp;origin_page_url=http%3A%2F%2Flocalhost%3A9000%2Fdemos%2F131%2F291edd87-c17e-422c-9bdc-f5686b23a6ed&amp;video_url=%2F%2Fwww.youtube.com%2Fembed%2FL_dNpV7AVc0" scrolling="no" width="600px" height="463px" frameborder="no"></iframe>
                    scope.widgetUrl = $window.location.origin + '/public/angularApps/index.html#/widgets/' + scope.widget.apiKey + '/view?since=' + new Date().getTime();
                }
                $scope.compileToText();
            });



            $scope.compileToText = function () {
                if ($scope.asCode) {
                    $element.empty();
                    var compiledHTML = $compile(contents)($scope);
                    $timeout(function () { // need this because compiling is on event queue so we register right after it.http://stackoverflow.com/a/18600499/1068746
                        var outerHTML = compiledHTML[0].outerHTML;
                        var text = $('<textarea></textarea>', { 'html': outerHTML, 'rows' : 40, 'cols' : 40, width:'400px', 'height':'100px', 'disabled':'disabled'});
                        $element.append(text);



                        $log.info(outerHTML);
                    }, 0);
//                        $element.text($tmpDiv.text().html());
//                        $log.info($element.text());
                }
//                    $log.info($compile($element.contents())($scope));
//                    TextContentCompiler.asText($scope, $element, 'iframe', ['ng-src']);
            };

        }
    };
});