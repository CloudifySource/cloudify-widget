'use strict';

/** This is a holy-grail implementation **/
/** read more at :
 /** other reference implementations just in case :
 http://fiddle.jshell.net/teresko/EkTVv/show/
 http://alistapart.com/article/holygrail

 Use it wisely
 **/
angular.module('WidgetApp')
    .directive('layout', function ($log, ApplicationService, $location) {
        return {
            templateUrl: 'views/directives/layout.html',
            restrict: 'C',
            transclude: true,
            replace: true,
            link: function postLink(scope/*, element*//*, attrs*/) {
                scope.doLogout = function () {
                    ApplicationService.logout().then(function (/*result*/) {
                        $location.path('#/user/login');
                    });


                };

                ApplicationService.isLoggedIn().then(function( result ){
                    scope.loggedIn = result.data.loggedIn;
                    if ( !!scope.loggedIn && $location.path().indexOf('/users/login') === 0){
                        $location.path('/widgets/index');
                    }


                });
            }

        };
    });
