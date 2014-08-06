'use strict';


angular.module('WidgetApp').service('CloudProvidersService', function( $http ){

    this.list = function(){
        return $http.get('/cloudProviders/list');
    }


});