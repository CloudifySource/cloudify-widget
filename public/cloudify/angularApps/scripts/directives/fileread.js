'use strict';
// a directive that allows us to preview the icon.
angular.module('WidgetApp').directive('fileread', [function () {
    return {
        restrict:'CA',
        transclude:true,
        scope: {
            filemodel: '=',
            preview: '='
        },
        template: '<div ng-transclude></div>',
//        replace:true,
        link: function (scope, element/*, attributes*/) {
            console.log(['my scope',scope]);

            var reader = new FileReader();
            reader.onload = function (loadEvent) {
                scope.$apply(function () {
                    console.log('updating scope fileread');

                    scope.preview = loadEvent.target.result;
                });
            };
            if ( element.is('input')){ // file input.. listen on value changeevent
                console.log('setting up an input field');
                element.on('change', function (changeEvent) {
                    console.log('handling file changed');
                    scope.$apply(function () {
                        console.log('updating scope fileread');
                        scope.filemodel = changeEvent.target.files[0];
                    });


                    reader.readAsDataURL(changeEvent.target.files[0]);
                });

            }else{ // treat this as a drop zone
                console.log(['defining drop zone',element]);

                element.on('dragover',function(e){
//                    scope.$apply(function(){
//                        element.css('background-color','blue').css('height','200px');
                    e.preventDefault();
                    e.stopPropagation();
//                        console.log('hello world!!!!');
//                    });
//                    console.log(['caught dragover',e.dataTransfer, e.originalEvent.dataTransfer]);
                    return false;
                });
                $(element).on('drop', function(evt){
                    evt.stopPropagation();
                    evt.preventDefault();
                    console.log(['printing event',evt, evt.originalEvent.dataTransfer]);
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
    };
}]);