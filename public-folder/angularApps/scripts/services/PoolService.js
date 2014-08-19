'use strict';
angular.module('WidgetApp').service('PoolService', function ($http) {

    var authToken = $.cookie('authToken');

    this.getStatus = function ( ) {
        return $http(
            {
                'method' : 'GET',
                'url' : '/backend/application/pool/status',
                'params' : {
                    'authToken' : authToken
                }
            }
        );

    };
});