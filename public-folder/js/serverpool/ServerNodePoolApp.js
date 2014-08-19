/**
 * using chrome dev area with angular
 *              https://www.youtube.com/watch?feature=player_embedded&v=Klqn73uzQao
 *
 * @param $routeProvider
 */

var serverNodePoolConfig = function($routeProvider, $httpProvider){

    $routeProvider
        .when('/', {
            controller: 'ServerNodePoolController',
            templateUrl: '/public/pages/pool/manage/index.html'
        });

    $httpProvider.responseInterceptors.push('WidgetAjaxInterceptor');
};

var ServerNodePoolApp = angular.module( 'ServerNodePoolApp', ['ui.bootstrap','ui', 'WidgetModules', 'ngCookies'] ).config( serverNodePoolConfig );



ServerNodePoolApp.controller('ServerNodePoolController',
    function ($scope, $location, $routeParams, $dialog, $rootScope, $cookies, ServerNodePoolModel, SessionService) {

        SessionService.applySession( $scope ); // applies authToken and admin on scope.
        $scope.data = [];
        $scope.database = {};

        try {
            function safelyGetDataNodeId(nodeId) {
                if (!$scope.database.hasOwnProperty("node_" + nodeId)) {
                    $scope.database["node_" + nodeId] = {};
                    $scope.data.push($scope.database["node_" + nodeId]);
                }
                return $scope.database["node_" + nodeId];
            }

            function deleteNodeId(nodeId) {

                if ($scope.database.hasOwnProperty("node_" + nodeId)) {
                    var nodeToDelete = $scope.database["node_" + nodeId];
                    $scope.data.splice($scope.data.indexOf(nodeToDelete), 1);
                    delete $scope.database["node_" + nodeId];
                }
            }

            ServerNodePoolModel.getServerNodes($scope.authToken).then(function (data) {
                $.each(data, function (index, item) {
                    safelyGetDataNodeId(item.nodeId)["server"] = item
                });
            });
            ServerNodePoolModel.getCloudServers($scope.authToken).then(function (data) {
                $.each(data, function (index, item) {
                    safelyGetDataNodeId(item.id)["machine"] = item
                });
            });
            ServerNodePoolModel.getWidgetInstances($scope.authToken).then(function (data) {
                $.each(data, function (index, item) {
                    safelyGetDataNodeId(item.instanceId)["instance"] = item
                });
            });
            ServerNodePoolModel.getStatuses($scope.authToken).then(function (data) {
                $.each(data, function (index, item) {
                    if (item.instanceId != null) {
                        safelyGetDataNodeId(item)["status"] = item;
                    }
                });
            });
        } catch (e) {
            console.log(["error", e]);
        }

        $scope.checkAvailability = function( node ){
            ServerNodePoolModel.checkAvailability( $scope.authToken, node.server ).then ( function(result ){ node["check"] = result; });
        };

        $scope.removeNode = function( node ){
            if ( confirm("Are you sure you want to remove this server node? ") ){
                console.log("removing node");
                ServerNodePoolModel.removeNode( $scope.authToken, node.server);
            }else{
                console.log("not removing node");
            }
        };

        $scope.nodeId = function( node ){
            return ServerNodePoolModel.getNodeId( node );
        };

        $scope.addNode = function(){
            ServerNodePoolModel.addNode( $scope.authToken).then( function( result ){ console.log("added node to pool successfully. please refresh")});
        };


        $scope.events = [];

        function subscriber(){

            var callbacks = { MachineStateEvent : {
                      ERROR:function( msgObj ){
                          console.log("dealing with machine state error");
                      },
                      DELETE:function( msgObj ){
                          console.log(["machine was deleted", msgObj, msgObj.resource.id ]);
                          deleteNodeId(msgObj.resource.id);
                      },
                    UPDATE: function(msgObj){
                        console.log(["received update msg", msgObj]);
                        var machine = msgObj.resource;
                        safelyGetDataNodeId( machine.id )["machine"] = machine;
                    },
                    CREATE: function( msgObj ){
                        console.log(["received create msg", msgObj]);
                        var machine = msgObj.resource;
                        safelyGetDataNodeId( machine.id )["machine"] = machine;

                    }
            } } ;




            this.onclose = function(){ console.log("closing"); };
            this.onmessage = function( event ){
                try{
//                    debugger;
                    console.log("got message");

                    var messageObj = JSON.parse( event[0].data );
                    $scope.events.push(messageObj);
                    if ( callbacks.hasOwnProperty(messageObj.category) && typeof(callbacks[messageObj.category] == "function") && callbacks[messageObj.category].hasOwnProperty(messageObj.type) && typeof(callbacks[messageObj.category][messageObj.type] == "function")){
                        callbacks[messageObj.category][messageObj.type]( messageObj );
                    }else{
                        console.log(["I don't have a callback for this message", messageObj]);
                    }

                }catch(e){ console.log(["unable to handle websocket message",arguments, e])}
            };
            this.onopen = function(){ console.log("openning");};
        }

        ServerNodePoolModel.subscribe( $scope.authToken, new subscriber() );

    }
);

ServerNodePoolApp.service('WebSocketService', function ( $rootScope ){

    /**
     *
     * @param url - may be an "http://" or "https://" url - which will be replaced with "ws://" or "wss://" respectively.
     * @param listener - implementation for the WebSocket events - "message","open", "error","close"
     *
     * This service will invoke the listener within a $rootscope.$apply method.
     *
     */
    var self = this;
    this.subscribe = function( url, listener ){
        var webSocketUrl =  url;
        if ( webSocketUrl.indexOf("http://") == 0 ){
            webSocketUrl = webSocketUrl.replace("http://","ws://");
        }

        function invokeListener( funcName, args ){
            if ( listener.hasOwnProperty(funcName) && typeof(listener[funcName]) == "function"){
                listener[funcName](args);
            }
            $rootScope.$apply();
        }

        console.log(["registering on ",webSocketUrl]);
        var webSocket = new WebSocket(webSocketUrl);
        listener.webSocket = self;
        webSocket.onclose = function(){
            console.log(["websocket onclose", arguments]);
            invokeListener("onclose", arguments );

        };

        webSocket.onmessage = function( event ){
            console.log(["websocket onmessage", arguments, event.data]);
            invokeListener("onmessage", arguments);
        };

        webSocket.onopen = function(){
            console.log(["on open", arguments]);
            invokeListener("onopen", arguments );
        };

        this.send = function( ){
            console.log(["sending", arguments ]);
            webSocket.send( arguments );
        }
    }

});

ServerNodePoolApp.service('ServerNodePoolModel', function( $http, WebSocketService ){

    this.getNodeId = function( node ){

        return !!node.machine ? node.machine.id : node.server.nodeId ;
    };

    this.getServerNodes = function( authToken ){
        console.log(['getting all server nodes', authToken]);
        return $http.get(jsRoutes.controllers.AdminPoolController.getServerNodes( authToken ).url ).then(function( data ){ return data.data; });
    };

    this.getWidgetInstances = function( authToken ){
        return $http.get( jsRoutes.controllers.AdminPoolController.getWidgetInstances( authToken).url).then( function(data){ return data.data });
    };

    this.getCloudServers = function( authToken ){
        return $http.get( jsRoutes.controllers.AdminPoolController.getCloudServers( authToken).url).then( function(data){ return data.data });
    };

    this.getStatuses = function( authToken ){
        return $http.get( jsRoutes.controllers.AdminPoolController.getStatuses( authToken).url).then( function(data){ return data.data });
    };

    this.checkAvailability = function( authToken , server ){
        return $http.get( jsRoutes.controllers.AdminPoolController.checkAvailability( authToken, server.nodeId).url).then( function(data){ return data.data } );
    };

    this.removeNode = function( authToken, server ){
        return $http.post( jsRoutes.controllers.AdminPoolController.removeNode( authToken, server.nodeId).url).then( function(data){ return data.data });
    };

    this.addNode = function( authToken ){
        return $http.post( jsRoutes.controllers.AdminPoolController.addNode( authToken ).url).then( function(data){ return data.data });

    };

    this.subscribe = function( authToken, listener ){
         WebSocketService.subscribe( jsRoutes.controllers.AdminPoolController.poolEvents(authToken).absoluteURL(), listener );

    }

});
