'use strict';

angular.module('WidgetApp').service('i18n', function ($http, $rootScope) {

    var option = {
        lng: 'en',
        fallbackLng: 'en',
        resGetPath: '/public-folder/js/i18next/dicts/__ns__-__lng__.json'
    };

    function init() {
        i18n.init(option, function () {
            $rootScope.$digest();
            console.log('after i18n loading');
        });
    }

    init();

    this.translate = function (key) {
        return typeof(key) === 'undefined' ? undefined : window.i18n.t(key);
    };

    this.setLanguage = function( language ){
        option.lng = language;
        init();
    };
});