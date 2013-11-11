/**
 *
 * This file contains reusable modules across the application
 *
 **/

var WidgetModules = angular.module("WidgetModules",["gs.modules"]);
WidgetModules.controller("PoolSummaryController",function( $scope, SessionService, WidgetModel ){
    SessionService.applySession( $scope );
    $scope.summary = WidgetModel.getSummary( $scope.authToken );
});


WidgetModules.factory( 'WidgetAjaxInterceptor', function ( $rootScope, $q, $window )
{
    function success( response )
    {
        $rootScope.AjaxCallInProcess = false;
        $rootScope.formErrors = {};
        return response;
    }

    function error( response )
    {
        $rootScope.AjaxCallInProcess = false;
        var status = response.status;
        if ( status == 401 ) {
            window.location = "/";
            return;
        }

        var hdrs = response.headers();
        if ( hdrs["display-message"]){
            var displayMessages = JSON.parse(hdrs["display-message"]);
            if ( displayMessages["formErrors"]){
                $rootScope.formErrors = displayMessages["formErrors"];
            }
        }
        console.log(["hdrs",hdrs]);
        // otherwise
        return $q.reject( response );
    }

    return function ( promise )
    {
        $rootScope.AjaxCallInProcess = true;
        return promise.then( success, error );
    };
} );

console.log("loading SessionController");
WidgetModules.service("SessionService", function( WidgetCookies ){

    this.applySession = function ($scope) {
        try {
            $scope.authToken = WidgetCookies.authToken();
            $scope.admin = WidgetCookies.admin();
            console.log("loaded SessionController successfully");
        } catch (e) {
            console.log("error while loading SessionController")
        }
    }

});

// a small service to reuse cookies.
// TODO : turn all cookies to encrypted.. requires entire application to switch to AngularJS. (login page included).
WidgetModules.service("WidgetCookies", function( $http, $q ){

    var cookies = {
        "admin": {
            "get": function(value){ return value == "true"}
        },
        "newDashboard": {},
        "authToken" : {},
        "cloudCredentials" : { "encrypt":true }

    };



    for ( var cookieName in cookies ){
        this[cookieName] = function(value){
            var cookie = cookies[cookieName];
            function doEncrypt( cookie ){
                return cookie.hasOwnProperty("encrypt") && cookie.encrypt;
            }

            var r = $q.defer();
            r.resolve($.map( cachedResults, function(v,k){ return v; }));

            if ( typeof(value) == "undefined"){ // get
                var cookieValue = $.cookie(cookieName);

                if ( cookies[cookieName].hasOwnProperty("get")){
                    return cookies[cookieName]["get"](cookieValue);
                }
            }else{ // set
                // currently no support for "set" method.. I have no idea what is the use-case.
                if ( cookies[cookieName].hasOwnProperty("encrypt") && cookies[cookieName].encrypt ){

                }
            }
            return r.promise;
        }
    }

    this.newDashboard = function(){
         return $.cookie('newDashboard') != null;
     };

    this.authToken = function(){ return $.cookie("authToken"); };
    this.admin = function(){ return $.cookie("admin") == "true"; };

    // currently only for cookies, but we might want to consider taking this logic out to "ApplicationService".
    function encrypt( cookieValue){
        return $http.get( jsRoutes.controllers.Application.encrypt(cookieValue).then(function(data){ return data.data; }) );
    };

    function descrypt( cookieValue ){
        return $http.get( jsRoutes.controllers.Application.decrypt(cookieValue).then(function(data){return data.data}));
    };

});


/**
 *
 * Guy - we are currently using promises here. We could take another approach - create an array and keep the reference
 * and then update the array - while keeping the same reference.
 *
 * something like
 *
 *           function( authToken, $scope ){
 *              $http().success(function(data){ $scope["widgets"] = data } )
 *          }
 *
 *
 * or
 *
 *         function (authToken){
 *              var result = {}
 *              $http().success(function(data){ result["widgets"] = data } }
 *              return result;
 *         }
 *
 *
 * The "result" here behaves like a scope that we can modify
 * This approach (updating references) might expose a nicer API.. at the meantime I am going with the flow and I use promises.
 *
 */


WidgetModules.service('WidgetModel', function( $http ){
    this.getWidgets = function( authToken ){
        console.log(["getting all widgets", authToken]);
//        return $http.get(jsRoutes.controllers.WidgetAdmin.getAllWidgets( "gergerge" ).url )
//            .success(function(a,b,c,d){ debugger; return "guy"})
//            .error(function(a,b,c,d){  debugger;});
        return $http.get(jsRoutes.controllers.WidgetAdmin.getAllWidgets( authToken ).url ).then(function( data ){ return data.data; }); //, function(a,b,c,d){  console.log("got an error"); });
    };

    this.saveWidget = function( authToken, widget, file ){
        var payload = new FormData();
        if( !!file ){
            if ( file === "remove" ){
                payload.append("removeIcon",true);
            }else{
                payload.append( "icon", file );
            }
        }
        payload.append( "authToken", authToken );
        payload.append( "widget", JSON.stringify(widget) );

// populate payload
        return $http.post( jsRoutes.controllers.WidgetAdmin.postWidget().url , payload, {
            headers: { 'Content-Type': false },
            transformRequest: function(data) { return data; }
        }).then( function(result){ return result.data });

    };

    this.getSummary = function (authToken ){
        console.log(["getting summary", authToken]);
        return $http.get(jsRoutes.controllers.AdminPoolController.summary( authToken ).url ).then(function(data){ console.log(["I have a summary", data]); return data.data.summary}, function(){ console.log("returning summary null" ); return null; }); // on error return null;
    };

    this.deleteWidget = function ( authToken , widget ){
        console.log(["deleting widget", authToken, widget]);
        return $http.post( jsRoutes.controllers.WidgetAdmin.deleteWidget( authToken, widget.apiKey ).url );
    };

    this.regenerateKey = function( authToken, widget ){

        return $http.post( jsRoutes.controllers.WidgetAdmin.regenerateWidgetApiKey(authToken, widget.apiKey ).url ).then( function(result){ return result.data.widget.apiKey; } );

    };

    this.enableWidget = function( authToken, widget ){
        return $http.post( jsRoutes.controllers.WidgetAdmin.enableWidget( authToken, widget.apiKey ).url );
    };

    this.disableWidget = function( authToken, widget ){
        return $http.post( jsRoutes.controllers.WidgetAdmin.disableWidget( authToken, widget.apiKey ).url );
    };



});


/**********************
 *
 *
 * Below this line we have modules that can be reused across Gigaspaces.
 * Due to lack of GS UI Infrastructure project right now, we place the code here.
 *
 *
 **********************/

// i18n plugin
angular.module("gs.modules", ["gs.modules.i18n","ui.utils.time"]);
angular.module('gs.modules.i18n',[] ).filter( 'i18n', function( i18n ){
    return function(key){ return i18n.translate(key); }
} ).service('i18n', function( $http, $rootScope ){
        var option = { lng:'dev', resGetPath: '/public/js/i18next/dicts/__ns__-__lng__.json' };
        i18n.init(option, function(){ $rootScope.$digest(); console.log("after i18n loading")});
        this.translate = function(key){ return typeof(key) =="undefined" ? undefined :  window.i18n.t(key) };
    } );
angular.module('ui.utils.time',[]).service('UiTimeUtils', function(){
    /*
     * JavaScript Pretty Date
     * Copyright (c) 2011 John Resig (ejohn.org)
     * Licensed under the MIT and GPL licenses.
     */

    // Takes an ISO time and returns a string representing how
    // long ago the date represents.
    this.prettyDate = function(time){
        var date = new Date((time || "") /*.replace(/-/g,"/").replace(/[TZ]/g," "))*/),
            diff = (((new Date()).getTime() - date.getTime()) / 1000),
            day_diff = Math.floor(diff / 86400);

        if ( isNaN(day_diff) || day_diff < 0 || day_diff >= 31 )
            return;

        return day_diff == 0 && (
            diff < 60 && "just now" ||
                diff < 120 && "1 minute ago" ||
                diff < 3600 && Math.floor( diff / 60 ) + " minutes ago" ||
                diff < 7200 && "1 hour ago" ||
                diff < 86400 && Math.floor( diff / 3600 ) + " hours ago") ||
            day_diff == 1 && "Yesterday" ||
            day_diff < 7 && day_diff + " days ago" ||
            day_diff < 31 && Math.ceil( day_diff / 7 ) + " weeks ago";
    }
}).filter('prettyDate', function (UiTimeUtils) {
        return function (date) {
            return UiTimeUtils.prettyDate(date);
        }
    }
);
