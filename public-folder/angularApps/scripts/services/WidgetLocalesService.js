'use strict';


angular.module('WidgetApp').service('WidgetLocalesService', function( $q ){

    var locales = [
        {
            'id' : 'cn',
            'label' : 'Chinese'
        },
        {
            'id' : 'en',
            'label' : 'English'
        }
    ];

    this.getLocales = function(){
        var deferred = $q.defer();
        deferred.resolve( { 'data' : locales } );
        return deferred.promise;
    };

    this.getById = function( id ){
        return _.find(locales, {'id' : id});
    };


    this.getDefault= function(){
        return this.getById('en');
    };

});