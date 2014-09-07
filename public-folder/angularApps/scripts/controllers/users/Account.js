'use strict';

angular.module('WidgetApp').controller('UsersAccountCtrl', function($scope, $log, ApplicationService, WidgetsService){
    $scope.login = {};
    $scope.changePassword = function(){
        ApplicationService.changePassword( $scope.changePassword.currentPassword, $scope.changePassword.newPassword, $scope.changePassword.newPasswordAgain ).then(function( result ){
            $scope.changePassword.success = true;
            $scope.changePassword.statusMessage = result.data;

        }, function( result ){
            $scope.changePassword.success = false;
            $scope.changePassword.statusMessage = result.data;
        });
    };


    WidgetsService.users.getUserDetails().then(function(result){
        $scope.userDetails = result.data;
    }, function(result){
        toastr.error('unable to get user details', result.data);
    })

});