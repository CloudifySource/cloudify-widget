/**
 * using chrome dev area with angular
 *              https://www.youtube.com/watch?feature=player_embedded&v=Klqn73uzQao
 *
 * @param $routeProvider
 */

// TODO DRY up
angular.module('gs.modules', ['gs.modules.i18n']);
angular.module('gs.modules.i18n', []).filter('i18n',function (i18n) {
    return function (key) {
        return i18n.translate(key);
    }
}).service('i18n', function ($http, $rootScope) {
        var option = { lng: 'dev', resGetPath: '/public/js/i18next/dicts/__ns__-__lng__.json' };
        i18n.init(option, function () {
            $rootScope.$digest();
            console.log('after i18n loading')
        });
        this.translate = function (key) {
            return window.i18n.t(key)
        };
    }
);


var serverNodePoolConfig = function($routeProvider, $httpProvider){

    $routeProvider
        .when('/', {
            controller: 'ServerNodePoolController',
            templateUrl: '/admin/serverNodePoolTemplate'
        });

    $httpProvider.responseInterceptors.push('myInterceptor');
};

var ServerNodePoolApp = angular.module( 'ServerNodePoolApp', ['ui.bootstrap','ui', 'gs.modules', 'ngCookies'] ).config( serverNodePoolConfig );

ServerNodePoolApp.factory('myInterceptor', function ($rootScope, $q, $window) {
    function success(response) {
        $rootScope.formErrors = {};
        return response;
    }

    function error(response) {
        var status = response.status;
        if (status == 401) {
            window.location = '/';
            return;
        }

        var hdrs = response.headers();
        if (hdrs['display-message']) {
            var displayMessages = JSON.parse(hdrs['display-message']);
            if (displayMessages['formErrors']) {
                $rootScope.formErrors = displayMessages['formErrors'];
            }
        }
        console.log(['hdrs', hdrs]);
        // otherwise
        return $q.reject(response);
    }

    return function (promise) {
        return promise.then(success, error);
    };
});

ServerNodePoolApp.controller('ServerNodePoolController',
    function ($scope, $location, $routeParams, $dialog, $rootScope, $cookies, ServerNodePoolModel) {

        $scope.authToken = $cookies.authToken;
        $scope.serverNodes = ServerNodePoolModel.getServerNodes($scope.authToken);

    }
);

ServerNodePoolApp.service('ServerNodePoolModel', function( $http ){

    this.getServerNodes = function( authToken ){
        console.log(['getting all server nodes', authToken]);
        return $http.get(jsRoutes.controllers.WidgetAdmin.getAllServers( authToken ).url ).then(function( data ){ return data.data; });
    };

    this.getSummary = function (authToken ){
        console.log(['getting summary', authToken]);
        return $http.get(jsRoutes.controllers.WidgetAdmin.summary( authToken ).url ).then(function(data){ console.log(['I have a summary', data]); return data.data.summary}, function(){ console.log('returning summary null' ); return null; }); // on error return null;
    };

});
