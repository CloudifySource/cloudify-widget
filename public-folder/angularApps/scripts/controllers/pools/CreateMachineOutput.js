'use strict';

angular.module('WidgetApp').controller('PoolsCreateMachineOutputCtrl', function ( $scope , CreateMachineOutputService ) {

    function _success(message){
        return function() {
            toastr.success(message);
        };
    }

    function _error(message){
        return function(result) {
            toastr.error(message,result.data);
        };
    }

    function _index(){
        CreateMachineOutputService.index().then(function(result){
            $scope.errors = result.data;
        },_error('unable to get list'));
    }

    function _deleteError(id){
        if ( confirm('delete error?')) {
            CreateMachineOutputService.deleteError(id).then(_success('deleted error successfully'), _error('could not delete error')).then(_index);
        }
    }

    function _deleteAll(){
        if ( confirm('delete all errors?')) {
            CreateMachineOutputService.deleteAll().then(_success('delete all'), _error('could not delete all')).then(_index);

        }

    }


    function _markAllRead (){
        CreateMachineOutputService.markAllRead().then(
            function(result){
                var updatedCount = parseInt( result.data.updated,10);
                if (  !isNaN(updatedCount) && updatedCount  > 0 ) {
                    _success('marked all errors as read')();
                }
            },
            _error('unable to update errors'));
    }

    _index();
    _markAllRead();

    $scope.deleteAll = _deleteAll;
    $scope.deleteError = _deleteError;


});