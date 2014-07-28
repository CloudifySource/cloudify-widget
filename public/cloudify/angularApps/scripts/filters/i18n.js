'use strict';
angular.module('WidgetApp').filter( 'i18n', function( i18n ){
    return function(key){ return i18n.translate(key); };
} );