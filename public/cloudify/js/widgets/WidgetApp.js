/**
 * This is the first AngularJS code sample in the project.
 *
 * I suggest the following resources for reading :
 *
 * http://architects.dzone.com/articles/promises-and-deferred-objects
 * http://docs.angularjs.org/api/ng.$http
 *
 * form validity-
 *      http://stackoverflow.com/questions/12503301/how-to-write-a-generic-error-handler-for-angularjs-forms
 *      http://stackoverflow.com/questions/14543437/angularjs-form-validation-with-directives-myform-valid-not-quite-right-for
 *
 *
 * using chrome dev area with angular
 *              https://www.youtube.com/watch?feature=player_embedded&v=Klqn73uzQao
 *
 * TODO - I need to add interceptors here for HTTP 401 and other error codes.
 * TODO - use angularJS resource http://docs.angularjs.org/api/ngResource.$resource
 *
 * @param $routeProvider
 */
var widgetConfig = function($routeProvider, $locationProvider, $httpProvider){
    $routeProvider
        .when('/', {
            controller: 'WidgetController',
            templateUrl: '/user/widgetsTemplate'
//            templateUrl: '/public/templates/widgets.html'
        });


        $httpProvider.responseInterceptors.push('myInterceptor');
};



var WidgetApp = angular.module( 'WidgetApp', ["ui.bootstrap","ui", "gs.modules"] ).config( widgetConfig );

WidgetApp.factory( 'myInterceptor', function ( $rootScope, $q, $window )
{
    function success( response )
    {
        $rootScope.formErrors = {};
        return response;
    }

    function error( response )
    {
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
        return promise.then( success, error );
    };
} );

WidgetApp.controller('WidgetController',
    function($scope, $location, $routeParams, $dialog, $rootScope,  WidgetModel ){

        $scope.authToken = $.cookie( "authToken" );
        WidgetModel.getWidgets( $scope.authToken ).then( function(data){ $scope.widgets = data; });
        $scope.admin = $.cookie("admin") == "true";
        $scope.summary = WidgetModel.getSummary( $scope.authToken );
        $scope.host = window.location.host;

        // edit it to edit the new widget form tips
        $scope.infoTooltips = {
                productName: "The name of your product",
                productVersion: "The version of your product",
                title: "The widget title as it will appear when displaying the widget within a web page",
                youtubeVideoUrl: "URL of a YouTube video you want to display within the widget (Optional)",
                providerURL: "The URL of the product owner, e.g. http://www.mongodb.org",
                recipeURL: "A URL (http/https)to the recipe zip file",
                consolename: "The title of the link to the product dashboard / UI in the widget console",
                consoleurl: "The URL to the product dashboard / UI. Use $HOST as the hostname placeholder, e.g.: http://$HOST:8080/tomcat/index.html"
            };

        $scope.newWidget = function(){ $scope.actions = { "editWidget" : {} } };
        $scope.goBack = function(){  $scope.showEmbedCode = false; $scope.actions = null; };
        $scope.viewInstances = function(widget){ $scope.actions = {"viewInstances" : widget }; };
        $scope.getEmbed = function( widget ){ $scope.showEmbedCode =  widget };
        $scope.editWidget = function( widget ){ $scope.actions = {"editWidget" : widget }; };
        $scope.delete = function(widget){

               var title = 'Are you sure?';
               var msg = 'Are you sure you want to delete widget : ' + widget.productName;
               var btns = [{result:'cancel', label: 'Cancel'}, {result:'ok', label: 'OK', cssClass: 'btn-primary'}];

               $dialog.messageBox(title, msg, btns)
                 .open()
                 .then(function(result){
                       if ( angular.isDefined(result ) ){
                           if ( result.toLowerCase() == "cancel"){
                               $scope.goBack();
                           }else if ( result.toLowerCase() == "ok"){
                                WidgetModel.deleteWidget( $scope.authToken, widget ).then(function(){
                                    // remove widget from widgets list.
                                    $scope.widgets = $($scope.widgets ).filter(function( index, item ){ return item.id != widget.id } ).toArray();
                                });
                           }
                       }
               });
        };

        $scope.regenerateKey = function(widget){
            console.log(["regenerating key",widget]);
            WidgetModel.regenerateKey( $scope.authToken, widget ).then( function(apiKey) { widget.apiKey =  apiKey; } );
        };

        $scope.disable = function(widget){
            console.log(["disabling widget",widget]);
            WidgetModel.disableWidget( $scope.authToken, widget).then( function(){ widget.enabled = false; });
        };

        $scope.enable = function(widget){
            console.log(["enabling widget",widget]);
            WidgetModel.enableWidget( $scope.authToken, widget ).then( function(){ widget.enabled = true });
        };

        $scope.shutDown = function( instance ){
            console.log(["shutting down instance", instance] );

        };

        $scope.saveWidget = function( widget, isDone ){
            var widgetFormConstruct = this.widgetForm;
            console.log(["saving widget", widget ]);
            WidgetModel.saveWidget( $scope.authToken, widget ).then( function( savedWidget ){
                widgetFormConstruct.$dirty = false; // ugly fix until angularjs patch this .
                if ( !angular.isDefined(widget.id)){
                    widget.id = savedWidget.id;
                }
                if ( isDone ){
                    $scope.goBack();
                }
            });
        };

        $scope.opts =  {
            backdropFade: true,
            dialogFade:true
          };
    }
);


angular.module("gs.modules", ["gs.modules.i18n"]);
angular.module('gs.modules.i18n',[] ).filter( 'i18n', function( i18n ){
    return function(key){ return i18n.translate(key); }
} ).service('i18n', function( $http, $rootScope ){
        var option = { lng:'dev', resGetPath: '/public/js/i18next/dicts/__ns__-__lng__.json' };
        i18n.init(option, function(){ $rootScope.$digest(); console.log("after i18n loading")});
        this.translate = function(key){ return window.i18n.t(key) };
    } );

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

WidgetApp.service('WidgetModel', function( $http ){
    this.getWidgets = function( authToken ){
        console.log(["getting all widgets", authToken]);
//        return $http.get(jsRoutes.controllers.WidgetAdmin.getAllWidgets( "gergerge" ).url )
//            .success(function(a,b,c,d){ debugger; return "guy"})
//            .error(function(a,b,c,d){  debugger;});
        return $http.get(jsRoutes.controllers.WidgetAdmin.getAllWidgets( authToken ).url ).then(function( data ){ return data.data; }); //, function(a,b,c,d){  console.log("got an error"); });
    };

    this.saveWidget = function( authToken, widget ){
        return $http.post(jsRoutes.controllers.WidgetAdmin.postWidget( authToken ).url, widget ).then( function( data ){ return data.data });
    };

    this.getSummary = function (authToken ){
        console.log(["getting summary", authToken]);
        return $http.get(jsRoutes.controllers.WidgetAdmin.summary( authToken ).url ).then(function(data){ console.log(["I have a summary", data]); return data.data.summary}, function(){ console.log("returning summary null" ); return null; }); // on error return null;
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
    }

});

WidgetApp.service( 'WidgetInstanceModel', function($http){
    this.shutdown = function(){

    }
});
