'use strict';
angular.module('WidgetApp').service('PoolService', function ($http) {



    this.cleanPool = function(){
        return $http.post('/backend/application/pool/clean');
    };

    this.getStatus = function ( ) {
        return $http.get('/backend/application/pool/status');

    };

    this.stopServerNode = function( serverNodeId ){
        return $http.post('/backend/application/pool/stopNode/' + serverNodeId );
    };

    this.getPoolNodesByStatus = function(){
        return $http.get('/backend/application/pool/nodesByStatus');
    };

    this.getDownloadRecipeTmpLink = function(){
        return $http.get('/backend/recipe/tmpDownloadLink');
    };


    this.getAllMachinesFromCloud = function(){
        return $http.get('/backend/pool/getAllMatchingTag');
    };

    this.stopCloudMachine = function(nodeId ){
        return $http.post('/backend/pool/stopCloudNode', { 'nodeId' : nodeId });
    };

});