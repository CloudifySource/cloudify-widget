'use strict';


angular.module('WidgetApp').service('WidgetThemes', function( $q ){

    var themes = [
        {
            'id' : 'ibm_en',
            'label' : 'IBM English'
        },
        {
            'id' : 'ibm_cn',
            'label' : 'IBM Chinese'
        }
    ];

    this.getThemes = function(){
        var deferred = $q.defer();
        deferred.resolve( { 'data' : themes } );
        return deferred.promise;
    };

    this.getById = function( id ){
        return _.find(themes, {'id' : id});
    };


    this.getDefault= function(){
        return this.getById('ibm_en');
    };

});