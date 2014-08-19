'use strict';

angular.module('WidgetApp').controller('WidgetsIndexCtrl', function ($scope, WidgetsService, $log, $location, PoolService) {
    $log.info('loading controller');

    function load() {

        PoolService.getStatus().then(function (result) {
            $scope.poolStatus = result.data;
        });

        WidgetsService.getWidgets().then(function (result) {
            $scope.widgets = result.data;

            if ($scope.widgets.length === 0) {
                $location.path('/widgets/welcome');
            }
        });
    }

    load();

    $scope.disable = function (widget) {
        console.log(['disabling widget', widget]);
        WidgetsService.disableWidget(widget).then(function () {
            widget.enabled = false;
        });
    };

    $scope.enable = function (widget) {
        console.log(['enabling widget', widget]);
        WidgetsService.enableWidget(widget).then(function () {
            widget.enabled = true;
        });
    };
});