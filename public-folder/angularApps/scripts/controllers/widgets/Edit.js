'use strict';

angular.module('WidgetApp').controller('WidgetsEditCtrl', function($scope, WidgetsService, $log, $location, $route,  $routeParams  ){
    $log.info('loading controller');

    $scope.logins = [
        {
            'id' : 'google',
            'label' : 'Google',
            'selected' : false
        },
        {
            'id' : 'custom',
            'label' : 'Custom',
            'selected' : false
        }
    ];


    $scope.navitems = [
        {
            'id' : 'general' ,
            'label' : 'General'
        },{
            'id' : 'icon' ,
            'label' : 'Icon'
        },{
            'id' : 'description' ,
            'label' : 'Description'
        },

        {
            'id':'data',
            'label' : 'Data'
        },
        {
            'id' : 'login',
            'label' : 'Login'
        },
        {
            'id' : 'cloudProvider',
            'label' : 'Cloud Provider'
        }
    ];

    var myPath = $location.path();



    $scope.$on('$locationChangeStart', function( e ){
        if ( !!$scope.isDirty && myPath !== $location.path() && myPath !== '/widgets/create') {
            if (!confirm('are you sure?')) {
                e.preventDefault();
            }
        }
    });

    $scope.navigateTo = function(item){
        $location.search('section', item.id);
//        $scope.currentSection = item;
    };

    function _doNavigation () {
        $scope.currentSection = _.find($scope.navitems, {'id': $routeParams.section || 'general'});
    }
    $scope.$watch( function(){ return $routeParams.section; },  _doNavigation );
    _doNavigation();


    $scope.$watch('logins', function(){
        if ( !$scope.logins || $scope.logins.length === 0 ){
            return;
        }
        var selected = [];
        for ( var i = 0; i < $scope.logins.length; i++ ){

            var login = $scope.logins[i];
            if ( !!login.selected ){
                selected.push(login.id);
            }
        }
        $scope.widget.loginsString = selected.join(',');

    },true);

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


    WidgetsService.cloud.listCloudNames().then(function(result){
        $scope.cloudNames = result.data;
    });

    WidgetsService.cloud.listCloudProviders().then(function(result){
        $scope.cloudProviders = result.data;
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

    updateWidgetData($scope.widget);


    var original = null;

    $scope.$watch('isDirty', function(newValue){
        if ( newValue === false ){
            original = JSON.stringify($scope.widget);
        }
    });

    function _setDirty( value ){
        $scope.isDirty = value;
    }

    function onWidgetLoad( result ){
        $scope.widget = result.data;
        updateWidgetData(result.data);

        $scope.$watch('widget', function( newValue/*, oldValue*/ ){
            if ( !!original ){
                _setDirty( JSON.stringify(newValue) !== original );
            }else{
                original = JSON.stringify($scope.widget);
            }
        },true);
    }

    var loadRequest = null;
    if ( !!$routeParams.widgetId ){
        loadRequest = WidgetsService.getWidget( $routeParams.widgetId);
    }else{
        loadRequest = WidgetsService.getWidgetDefaultValues();
    }

    loadRequest.then(onWidgetLoad);

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

    $scope.$watch(function(){return [$scope.widget,$scope.logins];}, function(){
        if ( !!$scope.widget && !!$scope.logins ){
            try{
                var logins = $scope.widget.loginsString && $scope.widget.loginsString.split(',') || '';
                _.each( logins, function(item){

                    var scopeLogin = _.find($scope.logins, {'id' : item });
                    if ( !!scopeLogin ){
                        scopeLogin.selected = true;
                    }
                });
            }catch(e){
                $log.error('unable to update logins',e);
            }


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

    $scope.saveWidget = function (widget, icon, isDone) {
        console.log(['saving widget', widget ]);
        WidgetsService.saveWidget(widget, icon).then(function (savedWidget) {
                toastr.success('widget saved successfully');

                $scope.errors = null;
                $scope.lastUpdated = new Date().getTime();

                $scope.widget = savedWidget;
                updateWidgetData(widget.data);
                _setDirty(false);
                if (!angular.isDefined(widget.id) || widget.id === null) {
                    $location.path('/widgets/' + savedWidget.id + '/edit');
                }
                if (isDone) {
                    $location.path('/widgets/' + $scope.widget.id + '/preview');
                }

            }, function (result) {
                var formErrors = result.data;
                for (var i in formErrors) {
                    if (formErrors.hasOwnProperty(i)) {
                        toastr.error(formErrors[i], i);
                    }
                }
                toastr.error('error while saving the widget', 'General');
            }
        );
    };


    $scope.runEmailTest = function(widget, testEmail){
        WidgetsService.sendInstallFinishedEmailTest( widget, testEmail).then(function(){
            toastr.success('email was sent successfully');
        },function( result ){
            toastr.error('error while sending email', result.data );
        });
    };


});