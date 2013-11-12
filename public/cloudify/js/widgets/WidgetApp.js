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
 * TODO - ** might not be relevant since we are using "type=file" input field and angularJS does not support it **
 *        use angularJS resource http://docs.angularjs.org/api/ngResource.$resource
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

// a directive that allows us to preview the icon.
WidgetApp.directive("fileread", [function () {
    return {
        restrict:'CA',
        transclude:true,
        scope: {
            filemodel: "=",
            preview: "="
        },
        template: '<div ng-transclude></div>',
//        replace:true,
        link: function (scope, element, attributes) {
            console.log(["my scope",scope]);

            var reader = new FileReader();
            reader.onload = function (loadEvent) {
                scope.$apply(function () {
                    console.log("updating scope fileread");

                    scope.preview = loadEvent.target.result;
                });
            };
            if ( element.is("input")){ // file input.. listen on value changeevent
                console.log("setting up an input field");
                element.on("change", function (changeEvent) {
                    console.log("handling file changed");
                    scope.$apply(function () {
                        console.log("updating scope fileread");
                        scope.filemodel = changeEvent.target.files[0];
                    });


                    reader.readAsDataURL(changeEvent.target.files[0]);
                });

            }else{ // treat this as a drop zone
                console.log(["defining drop zone",element]);

                element.on("dragover",function(e){
//                    scope.$apply(function(){
//                        element.css("background-color","blue").css("height","200px");
                        e.preventDefault();
                        e.stopPropagation();
//                        console.log("hello world!!!!");
//                    });
//                    console.log(["caught dragover",e.dataTransfer, e.originalEvent.dataTransfer]);
                    return false;
                });
                $(element).on("drop", function(evt){
                    evt.stopPropagation();
                    evt.preventDefault();
                    console.log(["printing event",evt, evt.originalEvent.dataTransfer]);
                    var files = evt.originalEvent.dataTransfer.files;
                    var count = files.length;
//
                    //Only call the handler if 1 or more files was dropped.
                    if (count > 0) {
                        reader.readAsDataURL(files[0]);
                        scope.filemodel = files[0];
                    }
                    return false;
                });
            }

        }
    }
}]);


WidgetApp.directive('img', function(){
    return {
        restrict:'E',
        link:function(scope,element){
            element.error(function(){
                $(this).hide();
            });
            element.load(function(){
                $(this).show();
            })
        }
    }
});



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

        $scope.newWidget = function(){ $scope.actions = { "editWidget" : {}, "editIcon":null } };
        $scope.goBack = function(){  $scope.showEmbedCode = false; $scope.actions = null; };
        $scope.viewInstances = function(widget){ $scope.actions = {"viewInstances" : widget }; };
        $scope.getEmbed = function( widget ){ $scope.showEmbedCode =  widget };
        $scope.editWidget = function( widget ){ $scope.actions = {"editWidget" : widget, "editIcon":null }; };
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
            if ( confirm('regenerating api key will break embedded instances for this widget') ){
                console.log(["regenerating key",widget]);
                WidgetModel.regenerateKey( $scope.authToken, widget ).then( function(apiKey) { widget.apiKey =  apiKey; } );
            }
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

        $scope.saveWidget = function( widget, icon, isDone ){
            console.log(["saving widget", widget ]);
            WidgetModel.saveWidget( $scope.authToken, widget, icon ).then( function( savedWidget ){
                $scope.lastUpdated=new Date().getTime();
                if ( !angular.isDefined(widget.id)){
                    // todo : test if we should override entire widget
                    widget.id = savedWidget.id;
                    widget.apiKey = savedWidget.apiKey;
                    $scope.widgets.push( savedWidget );
                }
                if ( isDone ){
                    $scope.goBack();
                }
            });
        };

        // support removing the icon by setting "remove" string. the service should translate this to the correct form field.
        $scope.removeIcon = function(){
            $scope.actions.editIcon="remove";
        };

        $scope.restoreIcon = function(){
            $scope.actions.editIcon = null;
        }

        $scope.widgetIconFile = "guy";
        $scope.$watch('widgetIconFile', function( oldValue, newValue ){
            console.log(["handling new value",oldValue,newValue]);
            debugger;
            if ( !!$scope.actions ){
                $scope.actions.editIcon = newValue;
            }
        });
        // support setting an icon to the widget. we just keep this to the scope. The file will be sent to server on save.
//        $("[type=file]").on('change',function(e){
//            $scope.$apply(function(){
//                $scope.actions.editIcon =(e.srcElement || e.target).files[0];
//            });
//        });

        $scope.opts =  {
            backdropFade: true,
            dialogFade:true
          };

    }
);