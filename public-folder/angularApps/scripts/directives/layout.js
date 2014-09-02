'use strict';

/** This is a holy-grail implementation **/
/** read more at :
 /** other reference implementations just in case :
 http://fiddle.jshell.net/teresko/EkTVv/show/
 http://alistapart.com/article/holygrail

 Use it wisely
 **/
angular.module('WidgetApp')
    .directive('layout', function ($log, ApplicationService, $location, $route, CreateMachineOutputService , $timeout ) {
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
                        'id' :'poolOutput',
                        'label' : 'Pool Output',
                        'url' : '#/pool/output'
                    },
                    {
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

                function loadUnreadOutput(){
                    CreateMachineOutputService.countUnread().then(function(result){

                        var count = parseInt(result.data.result,10);
                        var section = _.filter(scope.sections, {'id': 'poolOutput'})[0];
                        if ( !isNaN(count) && count > 0 ) {

                            section.tag = count;
                        }else{
                            section.tag = null;
                        }
                        $timeout(loadUnreadOutput,30000);
                    });
                }

                loadUnreadOutput();

                ApplicationService.isLoggedIn().then(function( result ){
                    scope.loggedIn = result.data.loggedIn;
                    if ( !!scope.loggedIn && $location.path().indexOf('/users/login') === 0){
                        $location.path('/widgets/index');
                    }


                });
            }

        };
    });
