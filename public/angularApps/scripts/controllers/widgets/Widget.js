'use strict';

angular.module('WidgetApp').controller('WidgetCtrl',function ($scope, $timeout, $log, $sce, $window, $routeParams, $filter, WidgetDbService, WidgetReceiveMessageService, WidgetsService, CloudTypesService, i18n, WidgetLocalesService ) {

    var apiKey = $routeParams.widgetKey;

    var recipeProperties = null;


    function _postMessage( data ){
        if ( typeof(data) !== 'string'){
            data = JSON.stringify(data);
        }
        $window.parent.postMessage(data, /*$window.location.origin*/ '*');
    }


    WidgetReceiveMessageService.addHandler( 'widget_recipe_properties', function(event){
        $log.info('got new properties for widget', event.data);
        recipeProperties = event.data;
    });

    $window.$windowScope = $scope;



    var hpcloud = 'HP';
    var softlayer = 'SOFTLAYER';
    var ec2 = 'AWS_EC2';

    var play = 'RUNNING';
    var stop = 'STOPPED';

    var popupWindow = null;

    $scope.loginDone = function( loginDetails ){
        if ( popupWindow !== null ){
            popupWindow.close();
        }

        $scope.loginDetails = loginDetails;
        $timeout(function(){$scope.play();}, 0);
    };



    $scope.widgetStatus = {};

    function _getAdvanced(){
        return $scope.advancedParams[$scope.cloudType];
    }

    $scope.advancedParams = {};
    $scope.advancedParams[hpcloud] = {'type':'hpcloud-compute','params':{'project':null, 'key':null, 'secretKey':null}};
    $scope.advancedParams[softlayer] = {'type':'softlayer', 'params' : {'username':null, 'apiKey':null} };
    $scope.advancedParams[ec2] = {'type':'aws_ec2', 'params' : {'key':null, 'secretKey':null} };

    $scope.poweredByUrl = {};
    $scope.poweredByUrl[hpcloud] = 'http://hpcloud.com';
    $scope.poweredByUrl[softlayer] = 'http://softlayer.com';
    $scope.poweredByUrl[ec2] = 'http://aws.amazon.com';

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


    function translateString( str ) {
        if ( str.indexOf('i18n') === 0 ) {
            return $filter('i18n')(str.substring('i18n:'.length));
        }
        return str;
    }

    function outputToSafe(status) {
        try {
            if (!!status.output && status.output.length > 0) {
                status.safeOutput = [];

                for (var i = 0; i < status.output.length; i++) {

                    var original = status.output[i];

                    try{
                        original = translateString(original);
                    }catch(e){
                        $log.warn(e);
                    }

                    status.safeOutput.push($sce.trustAsHtml(original));
                }
            }
        }catch(e){

        }
    }

    function resetWidgetStatus( msg ){
        $scope.widgetStatus = {
            'state' : stop,
            'reset':true
        };

        if ( !!msg ){
            $scope.widgetStatus.message = translateString(msg);
        }
        ellipsisIndex = 0;
    }



    var ellipsisIndex = 0;
    var ellipsis='......';

    function _scrollLog() {
        try {
            var log = $('#log')[0];
            log.scrollTop = log.scrollHeight;
        }catch(e){}
    }

    function handleStatus( status, myTimeout ){

        $log.info(['got status', status]);
        WidgetDbService.saveWidgetStatus( status );
        $scope.ellipsis = ellipsis.substring(ellipsis.length - ellipsisIndex % ellipsis.length);
        ellipsisIndex = ellipsisIndex +1;
        try {
            status.message = translateString(status.message);
        }catch(e){}
        $scope.widgetStatus = status;
        outputToSafe($scope.widgetStatus);
        $timeout(pollStatus, myTimeout || 3000) ;
        _scrollLog();

    }

    function pollStatus( myTimeout){

        if ( $scope.widgetStatus.state !== stop ){ // keep polling until widget stops ==> mainly for timeleft..
            WidgetsService.getStatus( $scope.widgetStatus.instanceId, apiKey ).then( function( result ){
                result = result.data;
                if ( !result ){
                    return ;
                }
                handleStatus( result.status, myTimeout );
            },function(result){
                $log.info(['status error',result]);
            });
        }else{
            $log.info('removing widget status');
            WidgetDbService.remove();
        }
    }


    $scope.isCompleted = function(){
        return $scope.widgetStatus.state === stop && !$scope.widgetStatus.instanceId && !$scope.widgetStatus.message && $scope.widgetStatus.timeleftMillis === 0;
    };
    $scope.pendingOutput = function(){
        return $scope.widgetStatus.state === play && !$scope.widgetStatus.output;
    };


    var popupWidths = {
        'google' : { 'width' : 400, 'height' : 500 },
        'custom' : { 'width' : 827 , 'height' : 376 }
    };

    $scope.play = function(){

        if ( $scope.isShowAdvanced() && !_hasAdvanced() ){
            $scope.advancedRequiredError = true;
            return;
        }
        $scope.advancedRequiredError = false;
        if ( !!$scope.widget.loginsString && !$scope.loginDetails ){
            var size = popupWidths[$scope.widget.loginsString];
            var left = (screen.width/2)-(size.width/2);
            var top = (screen.height/2)-(size.height/2);

            popupWindow = window.open( '/widget/login/' + $scope.widget.loginsString + '?widgetKey=' + apiKey  , 'Enter Details', 'toolbar=no, location=no, directories=no, status=no, menubar=no, scrollbars=no, resizable=no, copyhistory=no, width='+ size.width +', height='+ size.height +', top='+top+', left='+left);
            return;
        }
        $log.info('starting the widget');

        resetWidgetStatus();
        $scope.widgetStatus.state = play;

        var requestData = {};
        if ( !!_hasAdvanced() ){
            requestData.advancedData = _getAdvanced();
        }

        if ( !!recipeProperties ){
            requestData.recipeProperties = recipeProperties;
        }


        WidgetsService.play(  apiKey,  requestData )
            .then(function( result ){
                result = result.data;
                $log.info(['play result', result]);
                try {
                    result.status.message = translateString(result.status.message);
                }catch(e){

                }
                $scope.widgetStatus = result.status;
                outputToSafe($scope.widgetStatus);

                pollStatus(1);
            }, function( result ){
                $log.info(['play error', result]);
                resetWidgetStatus( $filter('i18n')('resetWidgetStatusMessage'));
            });
    };

    $scope.stop = function(){
        WidgetDbService.remove(); // remove the cookie
        _postMessage({name: 'widget_stop'});
        $scope.widgetStatus.state = stop;
        resetWidgetStatus();

    };

    $scope.getTimeLeft = function(){
        if ( !$scope.widgetStatus || !$scope.widgetStatus.timeleftMillis ){
            return '';
        }
        var timeLeft = $scope.widgetStatus.timeleftMillis;
        if ( !!timeLeft ){
            timeLeft = timeLeft / 1000;
            return ( Math.floor( parseInt(timeLeft,10) /60  ) + 1 );
        }
        return '';
    };

    $scope.showPlay = function(){
        return $scope.widgetStatus.state === stop;
    };

    $scope.showStop = function(){
        return $scope.widgetStatus.state === play;
    };

    $scope.submitAdvancedData = function(){
        $log.info('submitting advanced');
        $scope.play();
    };



    var savedStatus = WidgetDbService.getWidgetStatus();

    if ( !!savedStatus ){
        handleStatus( savedStatus,1 );
    }else{
        resetWidgetStatus();
    }


    $log.info(['saved status', savedStatus]);
    // place params on scope

    function widgetData(){
        try{
            if ( !!$scope.widget && !!$scope.widget.data ){
                if ( typeof($scope.widget.data) === 'string'){ // handle if data is string
                    return JSON.parse($scope.widget.data);
                }
                return $scope.widget.data; // simply return is data is an object
            }
        }catch(e){
            $log.error('unable to get widget data',e);
        }
        $scope.widget.data = {};
        return $scope.widget.data;

    }


    $scope.cloudType = CloudTypesService.getDefault().id;
    var language = WidgetLocalesService.getDefault().id;

    function setLanguage() {
        $log.info('changing language on widget');
        try {
            if (!!$scope.widget) { // if we have a widget on the scope ===> might be given from the outside

                var parsedData = widgetData();
                // ==> extract locale if exists
                if (!!parsedData.locale) {
                    language = parsedData.locale;
                }
            }
            i18n.setLanguage(language); // ==> set the new language
        }


        catch (e) {
            $log.info('failed to change language for widget',e);
        }
    }

    function updateSocialShares(){
        try{
            if ( !!$scope.widget ){
                if ( !$scope.widget.data.socialSources ){
                    $scope.widget.data.socialSources = [];
                }
               WidgetsService.shareSources.updateSocialSources( $scope.widget.data.socialSources  );
            }
        }catch(e){
            $log.info('failed to update social shares',e);
        }
    }

    setLanguage();

    $scope.$watch('widget', function( /*newValue*/ ){
        setLanguage();
        updateSocialShares();
//        $scope.cloudType = ( $scope.widget && $scope.widget.data && $scope.widget.cloudProvider ) || myConf.cloudProvider;
    });


    $scope.isShowAdvanced = function () {
//        $log.info(['showadvanced param is ', $scope.params.showAdvanced, $scope.params.showAdvanced == 'true']);
        try {
            return !!$scope.widget.showAdvanced;
        } catch (e) {
            return true;
        }
    };


    function shareUrl() {
        try {
            var widgetShareUrl = widgetData().shareUrl;
            if (!widgetShareUrl) {
                widgetShareUrl = document.referrer; // default to referrer url.
            }
            return encodeURI(widgetShareUrl);
        } catch (e) {
            $log.info('unable to get shareUrl', e);
        }
        return '';
    }

    function twitterMsg(){
        try{
            return encodeURIComponent(widgetData().twitterMessage);
        }catch(e){
            $log.info('unable to get twitter msg');
        }
        return '';
    }

    function renrenMsg(){
        try{
            return encodeURIComponent(widgetData().renrenMessage);
        }catch(e){
            $log.info('unable to get twitter msg');
        }
        return '';
    }

    function tencenWeiboTitle(){
        try{
            return encodeURIComponent(widgetData().tencenWeiboTitle);
        }catch(e){
            $log.info('unable to get twitter msg');
        }
        return '';
    }

    function sinaWeiboTitle(){
        try{
            return encodeURIComponent(widgetData().sinaWeiboTitle);
        }catch(e){
            $log.info('unable to get twitter msg');
        }
        return '';
    }

    function tecentWeiboSite(){
        try{
            return encodeURIComponent(widgetData().tecentWeiboSite);
        }catch(e){
            $log.info('unable to get twitter msg');
        }
        return '';
    }

    function getWechatUrl(){
        try{
            return encodeURI(widgetData().wechatUrl);
        }catch(e){
            $log.info('unable to get wechat url',e);
        }
    }

    function windowName(){
        return '_blank';
    }

    function getParamsOfShareWindow(width, height) {
        var popupParams =  ['toolbar=0,status=0,resizable=1,width=', width ,',height=' ,height , ',left=',(screen.width-width)/2,',top=',(screen.height-height)/2].join('');
        $log.info('popup params', popupParams);
        return popupParams;
    }
//    var msg =  encodeURI('Launch on the cloud in a single click using the Cloudify widget');


    // lets map each share source some widget relevant details
    // such as icon, open operation etc..

    // initialize the object with all shareSources
    var sourcesIds = WidgetsService.shareSources.SourcesIds;
    $scope.shareSources = [
        {
            'id' : sourcesIds.EMBED_CODE,
            'open' : function(){
                $scope.showEmbed = true;
            },
            'icon' : 'fa fa-share-alt-square'

        },
        {
            'id' :  sourcesIds.FACEBOOK,
            'open' : function(){
                window.open('http://www.facebook.com/sharer/sharer.php?u=' + shareUrl(), windowName(), getParamsOfShareWindow(640, 320));
            },
            'icon' : 'fa fa-facebook-square'
        },
        {
            'id': sourcesIds.GOOGLE_PLUS,
            'open' : function(){
                window.open('https://plus.google.com/share?url=' + shareUrl(), windowName(), getParamsOfShareWindow(640, 320));
            },
            'icon' : 'fa fa-google-plus-square'
        },
        {
            'id' : sourcesIds.TWITTER,
            'open' : function(){
                window.open('https://twitter.com/share?url=' + shareUrl() + '&text=' + twitterMsg(), windowName(), getParamsOfShareWindow(640, 320));
            },
            'icon' : 'fa fa-twitter-square'
        },
        {
            'id' : sourcesIds.LINKEDIN,
            'open' : function(){
                window.open('https://www.linkedin.com/cws/share?url=' + shareUrl(), windowName(), getParamsOfShareWindow(640, 320));
            },
            'icon' : 'fa fa-linkedin-square'
        },
        {
            'id' :  sourcesIds.RENREN,
            'open' : function(){
                window.open('http://share.renren.com/share/buttonshare?link=' + shareUrl() + '&title=' + renrenMsg(), windowName(), getParamsOfShareWindow(626, 436));
            },
            'icon' : 'fa fa-renren'
        },
        {
            'id' :  sourcesIds.WEIBO,
            'open' : function(){
                window.open('http://v.t.qq.com/share/share.php?title=' + tencenWeiboTitle() + '&url=' + shareUrl() + '&site=' + tecentWeiboSite(), windowName(), getParamsOfShareWindow(640, 320));
            },
            'icon' : 'fa fa-tencent-weibo'

        },
        {
            'id' : sourcesIds.SINA,
            'open' : function(){
                window.open('http://v.t.sina.com.cn/share/share.php?url=' + shareUrl() + '&title=' + sinaWeiboTitle(), windowName(), getParamsOfShareWindow(640, 320));
            },
            'icon' : 'fa fa-weibo'
        },
        {
            'id' : sourcesIds.WECHAT,
            'open' : function(){
                window.open(getWechatUrl(), windowName(), getParamsOfShareWindow(640,320));
            },
            'icon' : 'fa fa-wechat'
        }
    ];

    $scope.isShareSourceActive = function( shareSource ){
        try{

            var source = _.find(widgetData().socialSources, {'id' : shareSource.id});
            if ( !source){
                // by this point, the system and the widget configuration should be synced!
                // follow calls to WidgetShareSourcesService#updateSocialSources to see where we sync them
                $log.debug('share source is defined in system, but not on widget');
                return false;
            }
            return !!source.active;
        }catch(e){
            $log.info('unable to decide if share source active',e);
            return false;
        }
    };

});