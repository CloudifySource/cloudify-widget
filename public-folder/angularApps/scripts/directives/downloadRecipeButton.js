'use strict';
// a directive that allows us to preview the icon.
angular.module('WidgetApp').directive('downloadRecipeButton', function ( PoolService ) {
    return {

        restrict: 'CA',
        scope: {
            'deadline': '='
        },
        template: '<span><button ng-click="downloadRecipe()">Click here to download the recipe</button></span>',
        link: function (scope, $element/*, attributes*/) {

            scope.downloadRecipe = function(){
                PoolService.getDownloadRecipeTmpLink().then(function( result ){
                    toastr.success('download should begin soon');

                    var $a = $('<a></a>', {
                        'text': 'Download',
                        'target' : 'download',
                        'href' : result.data
                    });
                    $element.append($a);
                    $a[0].click(); // http://stackoverflow.com/a/980727
                    $a.remove();

                }, function( result ){
                    toastr.error('unable to get link', result.data);
                });
            };
        }
    };
});