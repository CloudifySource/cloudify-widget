'use strict';
angular.module('WidgetApp').service('MixpanelService', function( $log, $window ){

    // proper way to get parent's location - probably won't work 100% of the time, but acceptable for us.
    // http://stackoverflow.com/a/12198787
    var originPageUrl = $window.document.referrer || 'NA';

    if ( !window.mixpanel ){
        window.mixpanel = { track : function(){ $log.info(['mixpanel mock: tracking',arguments]);} };
    }

    this.stopWidget = function( pageTitle ){
        mixpanel.track('Stop Widget',{'page name' : pageTitle, 'url' : originPageUrl });
    };

    this.startWidget = function( pageTitle, isAnonymous ){
        mixpanel.track('Play Widget',{'page name' : pageTitle , 'url' : originPageUrl, 'anonymous' : isAnonymous });
    };

    this.trackClick = function( pageTitle, linkTitle ){
        mixpanel.track('Click', {'page name': pageTitle, 'url' : originPageUrl, 'link' : linkTitle  });
    };

});