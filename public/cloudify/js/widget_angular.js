var widgetModule = angular.module('widget', ['ngCookies']);

widgetModule.service('widgetReceiveMessageService', function( $log, paramsService ){

    var handlers = {};

    function addMessageHandler(  ){

        var originPageUrl = paramsService.params.origin_page_url;
        function receiveMessage(event)
        {
            if (event.origin !== originPageUrl ){
                $log.info('got event with origin ' + event.origin + ' which does not match param ' + originPageUrl );
                return;
            }else{
                try{
                    var recievedObj = event.data;
                    if ( recievedObj.hasOwnProperty('name')){
                        var name = recievedObj.name;
                        if ( handlers.hasOwnProperty( name )){
                            handlers[name](recievedObj);
                        }else{
                            $log.error('got an event with unknown name. I do not have a handler ' + name);
                        }
                    }else{
                        $log.error('received message without name ', event.data);
                    }
                }catch(e){
                    $log.error(e);
                }
            }

            // ...
        }
        window.addEventListener('message', receiveMessage, false);



    }

    this.addHandler = function(type, fn ){
        if ( handlers.hasOwnProperty(type)){
            $log.error('to handlers for type ' + type + ' I will disregard the second one');
        }
        handlers[type] = fn;
    }


});



widgetModule.controller('widgetCtrl', function ($scope, $timeout, $log, $window, widgetService, mixpanelService, paramsService, dbService) {

    $window.$windowScope = $scope;

    var hpcloud = 'HP';
    var softlayer = 'SOFTLAYER';
    var play = 'RUNNING';
    var stop = 'STOPPED';

    var popupWindow = null;

    $scope.loginDone = function( loginDetails ){
        if ( popupWindow !== null ){
            popupWindow.close();
        }

        $scope.loginDetails = loginDetails;
        $scope.play();
    };

    $scope.params = paramsService.params;
    $scope.widgetStatus = {};
    var widgetApiKey = $scope.params.apiKey;
    widgetService.getWidgetByApiKey(widgetApiKey).then(
        function success( result ){
            $log.info('widget with apiKey ' + widgetApiKey + ' loaded successfully');
            $scope.widget = result.data;
            $log.info($scope.widget);
        },
        function error( result ){
            $log.error('unable to load widget ' + widgetApiKey );
        }
    );

    function _getAdvanced(){
        return $scope.advancedParams[$scope.cloudType];
    }

    $scope.advancedParams = {};
    $scope.advancedParams[hpcloud] = {'type':'hpcloud-compute','params':{'project':null, 'key':null, 'secretKey':null}};
    $scope.advancedParams[softlayer] = {'type':'softlayer', 'params' : {'username':null, 'apiKey':null} };

    $scope.poweredByUrl = {};
    $scope.poweredByUrl[hpcloud] = "http://hpcloud.com";
    $scope.poweredByUrl[softlayer] = "http://softlayer.com";

    function _hasAdvanced(){
        var aData = _getAdvanced();
        var params = aData.params;
        for ( var i in params ){
            if ( params.hasOwnProperty(i) && !params[i] ){
                return false;
            }
        }
        return true;
    }


    function resetWidgetStatus( msg ){
        $scope.widgetStatus = {
            'state' : stop,
            'reset':true
        };

        if ( !!msg ){
            $scope.widgetStatus.message = msg;
        }
        ellipsisIndex = 0;
    }



    var ellipsisIndex = 0;
    var ellipsis='......';

    function _scrollLog() {
        var log = $('#log')[0];
        log.scrollTop = log.scrollHeight;
    }

    function handleStatus( status, myTimeout ){

        console.log(['got status', status]);
        dbService.saveWidgetStatus( status );
        $scope.ellipsis = ellipsis.substring(ellipsis.length - ellipsisIndex % ellipsis.length);
        ellipsisIndex = ellipsisIndex +1;
        $scope.widgetStatus = status;
        $timeout(pollStatus, myTimeout || 3000) ;
        _scrollLog();

    }

    function pollStatus( myTimeout){

        if ( $scope.widgetStatus.state !== stop ){ // keep polling until widget stops ==> mainly for timeleft..
            widgetService.getStatus( $scope.widgetStatus.instanceId, $scope.params.apiKey ).then( function( result ){
                if ( !result ){
                    return ;
                }
                handleStatus( result.status, myTimeout );
            },function(result){
                    console.log(['status error',result]);
                });
        }else{
            console.log("removing widget status");
            dbService.remove();
        }
    }


    $scope.isCompleted = function(){
        return $scope.widgetStatus.state === stop && !$scope.widgetStatus.instanceId && !$scope.widgetStatus.message && $scope.widgetStatus.timeleftMillis === 0;
    };
    $scope.pendingOutput = function(){
        return $scope.widgetStatus.state == play && !$scope.widgetStatus.output;
    };

    $scope.play = function(){

        if ( !!$scope.widget.loginsString && !$scope.loginDetails ){
            popupWindow = window.open("/widget/login/google","myWindow","width=400,height=500");    // Opens a new window
            return;
        }

        resetWidgetStatus();
        $scope.widgetStatus.state = play;

        var requestData = {};
        if ( !!_hasAdvanced() ){
            requestData["advancedData"] = _getAdvanced();
        }

        widgetService.play(  $scope.params.apiKey,  requestData )
            .then(function( result ){
                console.log(['play result', result]);
                $scope.widgetStatus = result.status;

                pollStatus(1);
        }, function( result ){
                console.log(['play error', result]);
                resetWidgetStatus("We are so hot that we ran out of instances. Please try again later.");
            });
//        success: function (data, textStatus, jqXHR) {
//            var state = data.status.state.toLowerCase();
//            if (state == "error" || state == "stopped") {
//                widgetState.showPlayButton();
//                widgetLog.error(data.status.message);
//                return;
//            }
//            if (data.status.instanceId) {
//                widgetState.instanceId(data.status.instanceId);
//            }
//
//            if ( advancedDataAvailable( advancedData ) ){
//                if (advancedData.project == advancedCookie.project() &&
//                    advancedData.key == advancedCookie.key() &&
//                    advancedData.secretKey == advancedCookie.secretKey()) {
//
//                    advancedCookie.prolong();
//
//                } else {
//                    $(".remember_creds").show();
//                }
//            }
//
//            setTimeoutForUpdateStatus(1);
//        },
//        error: function (data) {
//            var displayMessage = data.getResponseHeader("display-message");
//            if (displayMessage) {
//                var displayMessageObj = JSON.parse(displayMessage);
//                widgetLog.error(displayMessageObj.msg);
//            }
//        }
    };

    $scope.stop = function(){
        mixpanelService.stopWidget();
        $.postMessage( JSON.stringify({name:"widget_stop"}), $scope.params.origin_page_url , parent );
        $scope.widgetStatus.state = stop;
        resetWidgetStatus();
    };

    $scope.getTimeLeft = function(){
        if ( !$scope.widgetStatus || !$scope.widgetStatus.timeleftMillis ){
            return "";
        }
        var timeLeft = $scope.widgetStatus.timeleftMillis;
        if ( !!timeLeft ){
            timeLeft = timeLeft / 1000;
            return (parseInt(timeLeft /60 ) + 1 )+ " minutes";
        }
        return "";
    };

    $scope.showPlay = function(){
        return $scope.widgetStatus.state === stop;
    };

    $scope.showStop = function(){
        return $scope.widgetStatus.state === play;
    };

    $scope.submitAdvancedData = function(){
        console.log("submitting advanced");
        $scope.play();
    };



    var savedStatus = dbService.getWidgetStatus();

    if ( !!savedStatus ){
        handleStatus( savedStatus,1 );
    }else{
        resetWidgetStatus();
    }


    console.log(["saved status", savedStatus]);
    // place params on scope

    $scope.cloudType = myConf.cloudProvider;


    $scope.isShowAdvanced = function () {
//        console.log(["showadvanced param is ", $scope.params.showAdvanced, $scope.params.showAdvanced == "true"]);
        try {
            return $scope.params.showAdvanced == "true";
        } catch (e) {
            return true;
        }
    };

    var apiKey = $scope.params.apiKey;
    var shareUrl= encodeURI("http://bluforcloud.com/plans/play");

    var msg =  encodeURI("Launch on the cloud in a single click using the Cloudify widget");
    var twitterMsg = encodeURIComponent( "I just installed #ibmblu with a single click");

    $("#facebook_share_link").attr("href", "http://www.facebook.com/sharer/sharer.php?u=" + shareUrl).click(function(){
        window.open('http://www.facebook.com/sharer/sharer.php?u=' + shareUrl, 'facebook_share', 'height=320, width=640, toolbar=no, menubar=no, scrollbars=no, resizable=no, location=no, directories=no, status=no');
        return false;
    });
    $("#google_plus_share_link").attr("href", "https://plus.google.com/share?url=" + shareUrl).click(function(){
        window.open("https://plus.google.com/share?url=" + shareUrl, 'height=320, width=640, toolbar=no, menubar=no, scrollbars=no, resizable=no, location=no, directories=no, status=no');
    });
    $("#twitter_share_link").attr("href", "https://twitter.com/share?url=" + encodeURIComponent($scope.params.origin_page_url) + "&text=" + twitterMsg).click(function(){
        window.open("https://twitter.com/share?url=" + encodeURIComponent($scope.params.origin_page_url) + "&text=" + twitterMsg, 'height=320, width=640, toolbar=no, menubar=no, scrollbars=no, resizable=no, location=no, directories=no, status=no');
    });


});


// encrypt decrypt service
widgetModule.service('encryptDecrypt', function ($http) {
    this.encrypt = function (data) {
        return $http.post('/encrypt', {data: data });
    };

    this.decrypt = function (data) {
        return $http.post('/decrypt', {data: data})
    };
});


widgetModule.service('paramsService', function(){
    // read params from URL;
    function get_params() {
        var params = {};
        var hash;
        var hashes = window.location.search.substring(1).split('&');
        console.log(["using search tearm", hashes ]);
        for(var i = 0; i < hashes.length; i++)
        {
            hash = hashes[i].split('=');
            if ($.trim(hash[1]) != ""){
                params[hash[0]] = decodeURIComponent(hash[1]);
            }
        }
        console.log(["params are",params]);
        return params;
    }

    this.params = get_params();
});


widgetModule.service('dbService', function( $cookieStore, paramsService ){

    var params = paramsService.params;

    function getCookieName(){
        return "widgetCookie" + params.origin_page_url + params.apiKey;
    }


    this.remove = function(){
        this.saveWidgetStatus(null);
    };


    this.saveWidgetStatus = function(status){
        if ( !status ){
           $cookieStore.remove( getCookieName() );
        }else{
            $cookieStore.put( getCookieName() , status);
        }
    };

    this.getWidgetStatus = function(){
        return $cookieStore.get( getCookieName() );
    }
});


widgetModule.service('advancedData', function (encryptDecrypt) {
    var cookieName = "ADVANCED_DATA";
    this.save = function (data) {
        encryptDecrypt.encrypt(data).then(function (result) {
            $.cookie(cookieName, JSON.stringify(data), {"path": "/", expires: 10000 });
        });
    };

    this.load = function () {

        encryptDecrypt.decrypt($.cookie(cookieName)).then(function (result) {
            return result.data
        });
    }
});

widgetModule.service('widgetService', function( $http, mixpanelService, paramsService ){
    var origin_page_url = paramsService.params.origin_page_url;
    this.getStatus = function( instanceId , apiKey ){
        return $http.get( "/widget/"+ instanceId + "/status?apiKey=" + apiKey).then( function( data ){
            $.postMessage( JSON.stringify({name:"widget_status", data:data.data}), origin_page_url , parent );
            return data.data;
        });
    };


    this.play = function( apiKey, advancedData ){

        mixpanelService.startWidget( !advancedData );
        $.postMessage( JSON.stringify({name:"widget_play"}), origin_page_url , parent );
        if ( !advancedData ){
            return $http.post( '/widget/start?apiKey=' + encodeURI(apiKey)).then(function(result){ return result.data; });
        }else{
            return $http.post( '/widget/start?apiKey=' + encodeURI(apiKey),advancedData).then(function(result){ return result.data});
        }

    };

    this.getWidgetByApiKey = function(apiKey){
        return $http.get('/widget/' + apiKey + '/public');
    };

    this.stop = function(){
        if (!confirm("Are you sure you want to stop the instance?")) {
            return;
        }


        widgetState.showPlayButton();
        if ( widgetState.instanceId()) {
            $.post("/widget/"+ widgetState.instanceId() + "/stop?apiKey=" + params["apiKey"], {}, function (data) {
                if (data && data.status && data.status.state == "error") {
                    widgetState.showPlayButton();
                    widgetLog.error(data.status.message );
                    return;
                }
                stop_instance();
            });
        }
    }
});


widgetModule.service('mixpanelService', function( paramsService){
    var params = paramsService.params;

    if ( !window.mixpanel ){
        window.mixpanel = { track : function(){ console.log(["mixpanel mock: tracking",arguments])} };
    }

      this.stopWidget = function(){
            mixpanel.track("Stop Widget",{'page name' : params.title, 'url' : params.origin_page_url });
      };

    this.startWidget = function( isAnonymous ){
        mixpanel.track("Play Widget",{'page name' : params.title , 'url' : params.origin_page_url, "anonymous" : isAnonymous });
    };

    this.trackClick = function( linkTitle ){
        mixpanel.track("Click", {'page name':params.title, 'url' : params.origin_page_url, 'link' : linkTitle  })
    };

});


widgetModule.directive('track', function( mixpanelService, paramsService ){
    var params = paramsService.params;
    return {
        restrict:'C',
        link:function(scope,element){
            element.click(function(){
                $.postMessage( JSON.stringify({name:"widget_link_click", data:{"linkTitle" : element.text()}}), params.origin_page_url , parent );
                mixpanelService.trackClick( element.text() );
            })
        }
    }
});



