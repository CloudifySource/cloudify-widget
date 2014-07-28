
var angularAppConfig = function($routeProvider, $locationProvider, $httpProvider){
    $routeProvider
        .when('/', {
            controller: 'AccountCtrl',
            templateUrl: '/public/angularApps/views/account.html'
//            templateUrl: '/public/templates/widgets.html'
        });
};


angular.module( 'AccountApp', []).config(angularAppConfig);
console.log('hello world');



angular.module('AccountApp').controller('AccountCtrl', function($scope, $http, $log ){

//    POST    /admin/changePassword           controllers.WidgetAdmin.postChangePassword( authToken:String, oldPassword:String, newPassword:String, confirmPassword:String )
    var authToken = $.cookie('authToken');

    $scope.submitChangePasswordForm = function() {
        $log.info('submitting change password form');
        $http.post('/admin/changePassword', {
            'authToken': authToken,
            'oldPassword': $scope.data.oldPassword,
            'newPassword' : $scope.data.newPassword,
            'confirmPassword' : $scope.data.confirmPassword
        }).then(function(result){
            $scope.globalMessage = 'changed successfully';
        },  function(result){
                $scope.globalMessage = JSON.parse(result.headers('display-message')).msg;
            }

        );
    };
});