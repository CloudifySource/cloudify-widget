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
        $httpProvider.responseInterceptors.push('WidgetAjaxInterceptor');
};



console.log("loading WidgetApp");
var WidgetApp = angular.module( 'WidgetApp', ["ui.bootstrap","ui",   "WidgetModules"] ).config( widgetConfig );


WidgetApp.controller('WidgetController',
    function($scope, $location, $routeParams, $dialog, $rootScope,  WidgetModel, SessionService ){
        SessionService.applySession( $scope ); // apply session onto scope.

        $scope.host = window.location.host;

        WidgetModel.getWidgets( $scope.authToken ).then( function(data){ $scope.widgets = data; });

        // edit it to edit the new widget form tips
        $scope.infoTooltips = {
            recipeName: "The name in the recipe for uninstall.",
            consoleUrlService:"The name of the service replacing $HOST placeholder",
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
                if ( !angular.isDefined(widget.id)){
                    widget.id = savedWidget.id;
                    $scope.widgets.push( savedWidget );
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