'use strict';


// a directive that allows us to preview the icon.
angular.module('WidgetApp').directive('videoContainer', function ($sce) {
    return {
        restrict: 'A',
        transclude: true,

        scope: {
            'url': '='
        },
        replace: false,
//    <iframe src='http://localhost:9000/widget/291edd87-c17e-422c-9bdc-f5686b23a6ed/display' scrolling='no' width='600px' height='463px' frameborder='no'></iframe>
        templateUrl: 'views/directives/videoContainer.html',
//        replace:true,
        link: function (scope/*, element, attributes*/) {

            function isYouku(url) {
                return !!url && url.indexOf('youku') > 0;
            }

            function isYoutube(url) {
                return  !!url && url.indexOf('/embed/');
            }

//    http://v.youku.com/v_show/id_XNzM4NzQzMTIw.html?from=y1.3-idx-grid-1519-9909.86808-86807.3-1
            function getYoukuVideoKey(url) {
                if (isYouku(url)) {
                    return url.split('/id_')[1].split('.html')[0];
                }
                return null;
            }

            function getYoutubeVideoKey(url) {
                if (isYoutube(url)) {
                    return url.split('/embed/')[1];
                }
                return null;
            }


            scope.$watch('url', function () {

                scope.youkuKey  = getYoukuVideoKey(scope.url);
                scope.youtubeKey= getYoutubeVideoKey(scope.url);

                scope.youkuUrl = $sce.trustAsResourceUrl('http://player.youku.com/embed/' + scope.youkuKey );
                scope.youtubeUrl = $sce.trustAsResourceUrl('http://www.youtube.com/embed/' + scope.youtubeKey );
            });



        }
    };
});