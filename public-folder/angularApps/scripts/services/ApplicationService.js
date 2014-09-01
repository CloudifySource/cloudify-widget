'use strict';


angular.module('WidgetApp').service('ApplicationService', function( $http ){

    this.logout = function(){
        return $http.post('/backend/logout');
    };

    /**
     *
     * @param login  { 'email' : , 'password' : }
     */
    this.login = function( login ){
        return $http.post('/backend/login', login );
    };

    this.isLoggedIn = function(){
        return $http.get('/backend/isLoggedIn');
    };

    this.getUserDetails = function(){
        return $http.get('/backend/user/details');
    };


    this.changePassword = function( currentPassword, newPassword, newPassowrdAgain ){
        return $http.post('/backend/user/password/change', {'currentPassword' : currentPassword, 'newPassword' : newPassword , 'newPasswordAgain' : newPassowrdAgain });
    }
});