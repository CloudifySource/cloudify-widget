'use strict';

angular.module('WidgetApp').controller('DocsConfigurationCtrl', function ( $scope ) {


    $scope.configuration ={ 'children' :  [
        {
            name : 'server',
            children : [
                {
                    name : 'bootstrap',
                    children : [
                        {
                            'name' : 'createServerRetries',
                            'description' : 'The number of times to retry',
                            'type' : 'number',
                            'example' : 5
                        }
                    ]
                }
            ]

        }
    ] };
});