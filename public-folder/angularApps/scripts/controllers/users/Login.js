'use strict';

angular.module('WidgetApp').controller('UsersLoginCtrl', function($scope, $log, ApplicationService, $location ){
    $scope.login = {};
    $scope.doLogin = function(){
        ApplicationService.login( $scope.login).then(function(){
            $log.info('logged in successfully');
            $location.path('/widgets/index');
        }, function( result ){
            $log.info('error while logging in',result.data);
            $scope.loginError = true;
        });
    };

});