'use strict';
angular.module('WidgetApp').filter( 'duration', function( $log ){

    function padding(number){

        var numberLength = (number +'').length;
        return '00'.substring(numberLength) + number;
    }

    return function(millis) {
//        millis = 601157;
//        $log.info(millis);
        if (typeof(millis) !== 'number') {
            millis = parseInt(millis, 10);
        }

        if ( isNaN(millis)){
            return '';
        }

        return padding(Math.floor(millis / 60000)) + ':' + padding(Math.floor(millis / 1000 % 60));
    }
} );