'use strict';
angular.module('WidgetApp').service('PoolService', function ($http) {



    this.cleanPool = function(){
        return $http.post('/backend/application/pool/clean');
    };

    this.getStatus = function ( ) {
        return $http.get('/backend/application/pool/status');

    };

    this.getPoolNodesByStatus = function(){
        return $http.get('/backend/application/pool/nodesByStatus');
    };

    this.getBootstrapScript = function(publicIp, privateIp){
        return $http({
            'method' : 'GET',
            'url' : '/backend/application/bootstrap/script',
            'params' : {
                'publicIp' : publicIp || '1.1.1.1',
                'privateIp' : privateIp || '2.2.2.2'
            }
        });
    };
});