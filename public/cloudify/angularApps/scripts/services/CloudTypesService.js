'use strict';


angular.module('WidgetApp').service('CloudTypesService', function( $q ){

    var themes = [
        {
            'id' : 'SOFTLAYER',
            'label' : 'Softlayer'
        },
        {
            'id' : 'AWS_EC2',
            'label' : 'Amazon EC2'
        }
    ];

    this.getCloudTypes = function(){
        var deferred = $q.defer();
        deferred.resolve( { 'data' : themes } );
        return deferred.promise;
    };

    this.getById = function( id ){
        return _.find(themes, {'id' : id});
    };


    this.getDefault= function(){
        return this.getById('SOFTLAYER');
    };

});