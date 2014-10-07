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
        });
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


    $scope.stopCloudNode = function(cloudNode){
        cloudNode.status = 'user requested to stop this';
        cloudNode.canStop = false;
        PoolService.stopCloudMachine(cloudNode.id).then(function(){
            toastr.success('node ' + cloudNode.id +  ' stop request was sent successfully');
            cloudNode.status = 'stopped due to user request';
        }, function(result){
            toastr.error(result.data, 'error stopping cloud node');
            cloudNode.status = 'error while stopping';
            cloudNode.canStop = true;
        });
    };

    $scope.foundInDb = function(cloudNode){

        var filterFunc = function( poolNode ){ return poolNode.id === cloudNode.id; };

        if ( !!$scope.poolNodes ){
            return _.filter($scope.poolNodes.free || [], filterFunc).length > 0 ||
             _.filter($scope.poolNodes.occupied || [], filterFunc).length > 0;
        }else{
            return false;
        }

    };

    $scope.loadCloudNodes = function(){
        PoolService.getAllMachinesFromCloud().then(function(result){
            toastr.success('got cloud nodes');
            $scope.cloudNodes = result.data;
        }, function(result){
            toastr.error(result.data);
        });
    };

    $scope.downloadRecipe = function(){
      // guy - I know manipulating the dom from the controller is WRONG.. but using a directive approach in thie
    };



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