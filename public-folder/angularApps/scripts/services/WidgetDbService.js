'use strict';
angular.module('WidgetApp').service('WidgetDbService', function( $cookieStore, $routeParams  ){

    var apiKey = $routeParams.apiKey;
    var originPageUrl = document.referrer || 'NA';

    function getCookieName(){
        return 'widgetCookie' + originPageUrl + apiKey;
    }


    this.remove = function(){
        this.saveWidgetStatus(null);
    };


    function toCookieStatus( status ){
        return { 'state' : status.state , 'instanceId' : status.instanceId };
    }


    this.saveWidgetStatus = function(status){
        if ( !status ){
            $.cookie(getCookieName(), null, {'path': '/'});
        }else{
            $.cookie(getCookieName(), JSON.stringify(toCookieStatus(status)), {'path': '/', expires: 10000 });

        }
    };

    this.getWidgetStatus = function(){
        return $cookieStore.get( getCookieName() );
    };
});