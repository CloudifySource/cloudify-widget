'use strict';

angular.module('WidgetApp').controller('UsersAccountCtrl', function($scope, $log, ApplicationService){
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

});