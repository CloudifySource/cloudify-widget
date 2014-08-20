'use strict';
angular.module('WidgetApp').service('WidgetReceiveMessageService', function(  $log, $rootScope ){
    var handlers = {};

    function addMessageHandler(  ){
        function receiveMessage(event)
        {
            $log.debug('widget got message');
            try{
                var receivedObj = event.data;

                if ( typeof(receivedObj) ==='string' ){
                    receivedObj = JSON.parse(receivedObj);
                }

                if ( receivedObj.hasOwnProperty('name') && !!receivedObj.name ){
                    var name = receivedObj.name;
                    if ( handlers.hasOwnProperty( name )){
                        $rootScope.$apply(function(){
                            handlers[name](receivedObj);
                            $rootScope.modified = new Date().getTime();
                        });



                    }else{
                        $log.error('got an event with unknown name. I do not have a handler ' + name);
                    }
                }else{
                    $log.error('received message without name ', event.data);
                }
            }catch(e){
                $log.error(e);
            }
//            }
//
            // ...
        }
        $log.info('adding message handler');
        window.addEventListener('message', receiveMessage, false);
    }
    try{
        addMessageHandler();
    }catch(e){ $log.error(e);}

    this.addHandler = function(type, fn ){
        if ( handlers.hasOwnProperty(type)){
//            $log.error('there are two handlers for type ' + type + ' I will disregard the second one');
            return;
        }
        handlers[type] = fn;
    };
});