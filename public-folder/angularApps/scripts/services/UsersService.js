'use strict';
angular.module('WidgetApp').service('UsersService', function ($http) {

    this.getUserDetails = function(){
        return $http.get('/backend/user/details');
    };

});