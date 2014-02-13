var widgetModule = angular.module('widget', ['ngCookies']);





widgetModule.controller('widgetCtrl', function ($scope, $timeout, widgetService, mixpanelService, paramsService, dbService) {


    var hpcloud = 'HP';
    var softlayer = 'SOFTLAYER';
    var play = 'RUNNING';
    var stop = 'STOPPED';


    $scope.widgetStatus = {};

    function _getAdvanced(){
        return $scope.advancedParams[$scope.cloudType];
    }

    $scope.advancedParams = {};
    $scope.advancedParams[hpcloud] = {'type':'hpcloud-compute','params':{'project':null, 'key':null, 'secretKey':null}};
    $scope.advancedParams[softlayer] = {'type':'softlayer', 'params' : {'userId':null, 'apiKey':null} };

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
            widgetService.getStatus( $scope.widgetStatus.instanceId, $scope.params.apiKey ).success( function( result ){
                if ( !result ){
                    return ;
                }
                handleStatus( result.status, myTimeout );
            }).error(function(result){
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
        resetWidgetStatus();
        $scope.widgetStatus.state = play;
        debugger;
        widgetService.play(  $scope.params.apiKey,  _hasAdvanced() ? _getAdvanced() : null )
            .success(function( result ){
                console.log(['play result', result]);
                $scope.widgetStatus = result.status;

                pollStatus(1);
        }).error( function( result ){
                console.log(['play error', result]);
                resetWidgetStatus("unknown error");
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
        $scope.widgetStatus.state = stop;
        resetWidgetStatus();
    };

    $scope.getTimeLeft = function(){
        debugger;
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
    $scope.params = paramsService.params;
    $scope.cloudType = myConf.cloudProvider;

    var apiKey = $scope.params.apiKey;
    var shareUrl= encodeURI("http://launch.cloudifysource.org/d");

    var msg =  encodeURI("Launch on the cloud in a single click using the Cloudify widget");
    var twitterMsg = encodeURIComponent( $scope.params.title + " on any cloud with a single click with #cloudifysource");

    $("#facebook_share_link").attr("href", "http://www.facebook.com/sharer/sharer.php?u=" + shareUrl);
    $("#google_plus_share_link").attr("href", "https://plus.google.com/share?url=" + shareUrl);
    $("#twitter_share_link").attr("href", "https://twitter.com/share?url=" + encodeURIComponent($scope.params.origin_page_url) + "&text=" + twitterMsg);


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

widgetModule.service('widgetService', function( $http, mixpanelService ){
    this.getStatus = function( instanceId , apiKey ){
        return $http.get( "/widget/"+ instanceId + "/status?apiKey=" + apiKey );
    };

    this.play = function( apiKey, advancedData ){
        if ( !advancedData ){
            return $http.post( '/widget/start?apiKey=' + encodeURI(apiKey) );
        }else{
            return $http.post( '/widget/start?apiKey=' + encodeURI(apiKey),advancedData );
        }

    };

    this.stop = function(){
        if (!confirm("Are you sure you want to stop the instance?")) {
            return;
        }
        mixpanelService.stopWidget();
        $.postMessage( JSON.stringify({name:"stop_widget"}), origin_page_url , parent );
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
      this.stopWidget = function(){
            mixpanel.track("Stop Widget",{'page name' : params.title, 'url' : params.origin_page_url });
      };
});



