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
var widgetConfig = function($routeProvider){
    $routeProvider
        .when('/', {
            controller: 'WidgetController',
            templateUrl: '/user/widgetsTemplate'
//            templateUrl: '/public/templates/widgets.html'
        })
};

var WidgetApp = angular.module( 'WidgetApp', [] ).config( widgetConfig );

WidgetApp.controller('WidgetController',
    function($scope, $location, $routeParams, WidgetModel ){
        $scope.authToken = $.cookie( "authToken" );
        $scope.widgets = WidgetModel.getWidgets( $scope.authToken );
        $scope.admin = $.cookie("admin") == "true";
        $scope.summary = WidgetModel.getSummary( $scope.authToken );
        $scope.host = window.location.host;

        $scope.viewInstances = function(widget){
            console.log(["viewing instances", widget]);
            $("#running_instances_modal" ).modal('show'); // open dialog
            $scope.selectedWidget = widget;

        };

        $scope.regenerateKey = function(widget){
            console.log(["regenerating key",widget]);
        };

        $scope.disable = function(widget){
            console.log(["disabling widget",widget]);
        };

        $scope.enable = function(widget){
            console.log(["enabling widget",widget]);
        };

        $scope.delete = function(widget){
            console.log(["deleting widget",widget]);
        };

        $scope.requireLogin = function(widget){
            console.log(["require login", widget]);
        };

        $scope.getEmbed = function( widget ){
            $scope.selectedWidget = widget;
            $("#get_embed_modal" ).modal('show');
        };

        $scope.shutDown = function( instance ){
            console.log(["shutting down instance", instance]);

        };

        $scope.editWidget = function( widget ){
            $scope.selectedWidget = widget;
            $("#edit_widget_modal" ).modal("show");
        }
    }
);


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

    this.getSummary = function (authToken ){
        console.log(["getting summary", authToken]);
        return $http.get(jsRoutes.controllers.WidgetAdmin.summary( authToken ).url ).then(function(data){ console.log(["I have a summary", data]); return data.data.summary}, function(){ console.log("returning summary null" ); return null; }); // on error return null;
    }
});
