'use strict';

angular.module('WidgetApp').service('WidgetsDemoService', function( $http, $log) {

    this.getDemoWidgets = function( email ){
        return $http({
            'method' : 'GET',
            'url' : '/backend/demo/widget/list',
            'params' : {
                'email' : email
            }
        });
    }
});

