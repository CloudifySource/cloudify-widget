'use strict';

/** This is a holy-grail implementation **/
/** read more at :
 /** other reference implementations just in case :
 http://fiddle.jshell.net/teresko/EkTVv/show/
 http://alistapart.com/article/holygrail

 Use it wisely
 **/
angular.module('WidgetApp')
    .directive('layout', function ($log, ApplicationService, $location, $route ) {
        return {
            templateUrl: 'views/directives/layout.html',
            restrict: 'C',
            transclude: true,
            replace: true,
            link: function postLink(scope/*, element*//*, attrs*/) {

                scope.sections = [
                    {
                        'id' : 'widgets',
                        'label' : 'Widgets',
                        'url' : '#/widgets/index'
                    },
                    {
                        'id' : 'demo',
                        'label' : 'Demo',
                        'url' : '#/public/demo'

                    },{
                        'id': 'pool',
                        'label':'Pool',
                        'url' : '#/pool'
                    },{
                        'id': 'account',
                        'label':'Account',
                        'url' : '#/user/account'
                    },{
                        'id'  :'configuration',
                        'label' : 'Configuration',
                        'url' : '#/configuration'
                    }
                ];

                scope.isCurrentSection = function( section ){
                    return $route && $route.current && $route.current.locals && $route.current.locals.section === section.id ;
                };

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
