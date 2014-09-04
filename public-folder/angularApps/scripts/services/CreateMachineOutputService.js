'use strict';


angular.module('WidgetApp').service('CreateMachineOutputService', function ($http) {


    this.index = function () {
        return $http.get('/backend/createMachineOutput/index').then(function(result){
            _.each(result.data, function(item){
                item.contentObj = JSON.parse(item.content);
            });
            return result;
        });
    };

    this.deleteError = function (id) {
        return $http.post('/backend/createMachineOutput/' + id + '/delete');

    };

    this.deleteAll = function () {
        return $http.post('/backend/createMachineOutput/deleteAll');

    };


    this.countUnread = function () {
        return $http.get('/backend/createMachineOutput/countUnread');
    };

    this.markAllRead = function () {
        return $http.post('/backend/createMachineOutput/markAllRead');
    };


});