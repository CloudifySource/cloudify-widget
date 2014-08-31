'use strict';


angular.module('WidgetApp').service('WidgetsService', function( $http, $log, $cookies, WidgetThemes,
                                                                CloudTypesService, $window, MixpanelService,
                                                                CloudService,
                                                                WidgetsDemoService,
                                                                WidgetLocalesService, WidgetShareSourcesService  ){
    var authToken = $.cookie('authToken');
    $log.info('authToken is ', authToken);


    this.themes = WidgetThemes;
    this.cloudTypes = CloudTypesService;
    this.locales = WidgetLocalesService;
    this.shareSources = WidgetShareSourcesService;
    this.cloud = CloudService;

    this.demo = WidgetsDemoService;


    this.getWidgets = function( ){
        $log.info(['getting all widgets', authToken]);
        return $http({
            'method': 'GET',
            'url' : '/backend/widget/list',
            'params': {
                'authToken' : authToken

            }
        });
    };

    this.getWidgetDefaultValues = function(){
        return $http.get('/backend/widget/defaultValues');
    };


    this.getWidget = function( widgetId ){
        return $http({
            'method': 'GET',
            'url' : '/backend/widget/' + widgetId + '/get',
            'params' : {
                'authToken' : authToken
            }
        });
    };


    this.getWidgetByKey = function( widgetKey ){
        return $http({
            'method' : 'GET',
            'url' : '/backend/widget/' + widgetKey + '/public'
        });
    };

    this.saveWidget = function( widget, file ){
        var payload = new FormData();
        if( !!file ){
            if ( file === 'remove' ){
                payload.append('removeIcon',true);
            }else{
                payload.append( 'icon', file );
            }
        }
        payload.append( 'authToken', authToken );
        payload.append( 'widget', JSON.stringify(widget) );

// populate payload
        return $http.post( '/backend/widget/edit', payload, {
            headers: { 'Content-Type': undefined },
            transformRequest: function(data) { return data; }
        }).then( function(result){ return result.data; });

    };

//
    this.deleteWidget = function ( widget ){
        console.log(['deleting widget', authToken, widget]);
        return $http.post( '/backend/widget/' + widget.id + '/delete',  { 'authToken' : authToken } );
    };

//
    this.enableWidget = function( widget ){
        return $http.post( '/backend/widget/' + widget.id + '/enable', { 'authToken' : authToken } );
    };

    this.disableWidget = function( widget ){
        return $http.post( '/backend/widget/' + widget.id + '/disable' , {'authToken' : authToken } );
    };



    function _postMessage( data ){
        if ( typeof(data) !== 'string'){
            data = JSON.stringify(data);
        }
        $window.parent.postMessage(data, /*$window.location.origin*/ '*');
    }

    this.getStatus = function( instanceId , apiKey ){
        return $http.get( '/backend/widget/'+ instanceId + '/status?apiKey=' + apiKey).then( function( data ){
            _postMessage( {name:'widget_status', data:data.data});
            return data;
        });
    };


    this.play = function( apiKey, advancedData ){
        MixpanelService.startWidget( !advancedData );
        _postMessage( {name:'widget_play'} );
        if ( !advancedData ){
            return $http.post( '/backend/widget/start?apiKey=' + encodeURI(apiKey));
        }else{
            return $http.post( '/backend/widget/start?apiKey=' + encodeURI(apiKey),advancedData);
        }

    };

    this.stop = function( apiKey, instanceId ){
        if (!confirm('Are you sure you want to stop the instance?')) {
            return;
        }

        if ( !!instanceId ) {
            return $.post('/backend/widget/' + instanceId + '/stop?apiKey=' + apiKey);
        }
    };


    this.sendInstallFinishedEmailTest = function( widget, details){
        return $http.post('/backend/widget/' + widget.id + '/testInstallFinishedEmail', details);
    };


});