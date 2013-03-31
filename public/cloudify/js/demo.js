var widgetConfig = function($routeProvider){ $routeProvider.when( '/', { controller: 'DemoController', templateUrl: 'widgetTemplate' } ) };
var WidgetApp = angular.module( 'DemoApp', [] ).config( widgetConfig );
WidgetApp.controller('DemoController', function($scope, $location, $routeParams, $http ){
//    debugger;
    $scope["widgets"] = $http.get(jsRoutes.controllers.DemosController.listWidgetForDemoUser( $scope["userId"] ).url ).then(function(data){ $scope.menuClick(data.data[0]); return data.data });
    $scope.menuClick = function( widget ){
        $scope.selectedWidget = widget;
    };

//    $scope.menuClick( $scope.widgets[0]);
});




//$(function(){
//        var userId = $("body").attr("data-user-id");
//        jsRoutes.controllers.DemosController.listWidgetForDemoUser( $("body").attr("data-user-id")).ajax({
//            success:function( result ){
//                        console.log([result]);
//                        $(".demo-content").append($("#widget_nav").tmpl( {"widgets":result})).append($("#widgets_list").tmpl({"widgets":result, "userId":userId}));
//                        var $firstButton = $(".demo-link[data-api-key]:first");
//                        console.log([ $firstButton ]);
//                        $firstButton.click();
//                    },
//            error:function( result ){ console.log([result])}
//        });
//    $(".demo-link[data-api-key]").live("click", function(){
//        var $this = $(this);
//        var apiKey = $this.attr("data-api-key");
//        $(".recipe").hide();
//        $(".recipe[data-api-key=" + apiKey + "]").show();
//    });
//
//});