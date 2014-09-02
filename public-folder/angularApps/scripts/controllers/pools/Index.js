'use strict';

angular.module('WidgetApp').controller('PoolsIndexCtrl', function ( $scope , $q, $log, PoolService, $timeout ) {
    $scope.cleanPool = function(){
        $log.info('cleaning pool');
    };

    $scope.stopServerNode = function(serverNode){
        if ( !confirm('are you sure you want to stop this server node?')){
            return;
        }

        PoolService.stopServerNode(serverNode.modelId).then(function(){
            toastr.success('stopped successfully');
        },function( result ){
            toastr.error('error while stopping', result.data);
        })
    };

    $scope.cleanPool = function(){
        if ( !confirm ('are you sure you want clean the pool? this feature is not fully tested yet')){
            return;
        }
        else{
            PoolService.cleanPool().then(function(/*result*/){
                toastr.success('cleaned pool successfully. please wait a couple of minutes before you see machines are deleted');
            }, function(/*result*/){
                toastr.error('error while cleaning the pool. please talk to admin');
            });
        }
    };

    PoolService.getBootstrapScript().then(function(result){
        $scope.bootstrapScript = result.data;
    });



    var updateInterval = 10000;

    function loadStatus() {
        return $q.all([
            PoolService.getStatus(),
            PoolService.getPoolNodesByStatus()
        ])
            .then(function (res) {
                return {status: res[0], nodes: res[1]};
            }).then( function(result){
                $scope.nextUpdate = Date.now() + updateInterval;
                $scope.poolStatus = result.status.data;
                $scope.poolNodes = result.nodes.data;
                $timeout( loadStatus, updateInterval);
            });
    }

    loadStatus();
});