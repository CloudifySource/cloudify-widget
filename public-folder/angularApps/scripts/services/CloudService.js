'use strict';


angular.module('WidgetApp').service('CloudService', function( $http ){

    // lists the names of folders under cloudify/clouds directory.
    this.listCloudNames = function(){
        return $http.get('/backend/cloudNames/list');
    };

    // lists the supported cloud providers by the widget
    this.listCloudProviders = function(){
        return $http.get('/backend/cloudProviders/list');
    };


});