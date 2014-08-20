
'use strict';

angular.module('WidgetApp').controller('LoginsCustomCtrl', function ($scope, $location, $log, $routeParams, $http ) {

    $log.info('loading controller');

    $scope.widgetKey = $routeParams.widgetKey;

    function recoverLoginFromLocalStorage( ){
        try {
            if (typeof(Storage) !== 'undefined') {
                if (localStorage.hasOwnProperty('customLogin')) {
                    $scope.login = JSON.parse(localStorage.customLogin);
                }
                // Code for localStorage/sessionStorage.
                localStorage.setItem('lastname', 'Smith');
            }
        }catch(e){ $log.error('unable to recover login details', e);}
    }

    function saveLoginDetails(){
        if ( typeof(Storage) !== 'undefined' ){
            localStorage.customLogin = JSON.stringify($scope.login);
        }
    }


    $scope.login = {};
    recoverLoginFromLocalStorage();



    $scope.submitForm = function( ){
        $scope.error = null;
        saveLoginDetails();
        $log.info('submitting login', $scope.login);
        $http.post('/backend/widget/login/custom?widgetKey=' + $scope.widgetKey, $scope.login).then(function( result ){
            $log.info('got result from backend', result.data);

            window.opener.$windowScope.loginDone( $scope.login );

        }, function( result ){
            $log.info('got error from backend', $scope.error = result.data);
        });
    };
});