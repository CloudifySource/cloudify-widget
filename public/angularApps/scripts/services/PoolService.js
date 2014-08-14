'use strict';
angular.module('WidgetApp').service('PoolService', function ($http) {

    this.getStatus = function () {
        return $http.get('/backend/application/pool/status');

    };
});