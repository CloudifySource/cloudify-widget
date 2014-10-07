
'use strict';

angular.module('WidgetApp').controller('LoginsGoogleCallbackCtrl', function ($scope, $routeParams) {
    window.opener.$windowScope.loginDone(
        {
            'userId' : $routeParams.userId,
            'email': $routeParams.email,
            'name' : $routeParams.firstName,
            'lastName' : $routeParams.lastName
        } );
});