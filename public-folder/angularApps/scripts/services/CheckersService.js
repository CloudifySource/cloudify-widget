'use strict';

angular.module('WidgetApp').service('CheckersService', function( $http  ) {
    this.sendInstallFinishedEmailTest = function( widget, details){
        return $http.post('/backend/checkers/widget/' + widget.id + '/testInstallFinishedEmail', details);
    };

    this.checkAwsEc2ImageSharing = function( widget, details ){
        return $http.post('/backend/checkers/widget/'+ widget.id + '/testImageShareController',details);
    };
});