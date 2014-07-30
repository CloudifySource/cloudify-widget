'use strict';

angular.module('WidgetApp').controller('WidgetsEditCtrl', function($scope, WidgetsService, $log, $location, $routeParams  ){
    $log.info('loading controller');
    $scope.lastUpdated = new Date().getTime();

    WidgetsService.themes.getThemes().then(function( result ){
        $scope.themes = result.data;
    });

    WidgetsService.cloudTypes.getCloudTypes().then(function(result){
        $scope.cloudTypes = result.data;
    });

    WidgetsService.locales.getLocales().then(function(result){
        $scope.locales = result.data;
    });



    $scope.widget = { };
    $scope.widgetData = {};


    function updateWidgetData( widget ){

        $scope.widgetData = ( !!widget.data && JSON.parse(widget.data) ) || {};
        if ( !$scope.widgetData.socialSources ){
            $scope.widgetData.socialSources = [];
        }
        WidgetsService.shareSources.updateSocialSources($scope.widgetData.socialSources);


    }

    if ( !!$routeParams.widgetId ){
        WidgetsService.getWidget( $routeParams.widgetId).then(function(result){
            updateWidgetData(result.data);
            $scope.widget = result.data;

        });
    }

    $scope.$watch('widgetData', function(){
        $log.info('updating widget data', $scope.widgetData );

        $scope.widget.data = JSON.stringify($scope.widgetData);
        $log.info('widget.data is now' , $scope.widget.data);
    },true);

    $scope.$watch(function(){return [$scope.widget,$scope.themes];}, function(){
        if ( !!$scope.widget && !!$scope.themes && !$scope.widgetData.theme ){
            $scope.widgetData.theme = WidgetsService.themes.getDefault().id;
        }
    },true);


    $scope.$watch(function(){return [$scope.widget,$scope.cloudTypes];}, function(){
        if ( !!$scope.widget && !!$scope.cloudTypes && !$scope.widgetData.cloudType ){
            $scope.widgetData.cloudType = WidgetsService.cloudTypes.getDefault().id;
        }
    },true);

    $scope.$watch(function(){return [$scope.widget,$scope.locales];}, function(){
        if ( !!$scope.widget && !!$scope.locales && !$scope.widgetData.locale ){
            $scope.widgetData.locale = WidgetsService.locales.getDefault().id;
        }
    },true);

    // support removing the icon by setting 'remove' string. the service should translate this to the correct form field.
    $scope.removeIcon = function(){
        $scope.actions.editIcon='remove';
    };

    $scope.restoreIcon = function(){
        $scope.actions.editIcon = null;
    };

    $scope.getSocialSourceLabel = function(source){
        try {
            return WidgetsService.shareSources.getById(source.id).label;
        }catch(e){
            $log.error('unable to get label for source',source,e);
            return '';
        }
    };

    $scope.actions = { 'editWidget' : {}, 'editIcon':null };
    $scope.data = {};


    $scope.$watch('data', function(  newValue ){
        if ( !newValue || !newValue.widgetIconFile ){
            return;
        }
        console.log(['handling new value',newValue.widgetIconFile]);
        if ( !!$scope.actions ){
            $scope.actions.editIcon = newValue.widgetIconFile;
        }
    },true);

    $scope.saveWidget = function( widget, icon, isDone ){
        console.log(['saving widget', widget ]);
        WidgetsService.saveWidget( widget, icon ).then( function( savedWidget ){
            $scope.errors = null;
            $scope.lastUpdated=new Date().getTime();
            if ( !angular.isDefined(widget.id)){
                // todo : test if we should override entire widget
                widget.id = savedWidget.id;
                widget.apiKey = savedWidget.apiKey;
                widget.version = savedWidget.version;

                updateWidgetData(widget.data);
            }
            if ( isDone ){
                $location.path('/widgets/' + $scope.widget.id + '/preview');
            }
        },function(result){
                $scope.errros = result.data;
            }
        );
    };


});