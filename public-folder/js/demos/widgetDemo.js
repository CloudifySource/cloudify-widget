// a general file for demo javascript
$(function () {

    function documentOrigin(){
        if (!window.location.origin) {
            window.location.origin = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port: '');
        }
    }
    documentOrigin();

    var msgSource = $("#cloudify-widget").attr("src").split("/js")[0];
    console.log("registering event for " + msgSource);
    var callbacks = {};
    callbacks["require_login"] = function (e) {
        $("body").trigger("requireLogin");
    };
    callbacks["widget_status"] = function(e) {
        // bubble up to demo page
        $.postMessage( e.data, document.location.origin , parent );
    };
    $.receiveMessage(function (e) {
            console.log(["parent got the message", e]);
            try {
                var msg = JSON.parse(e.data);
                console.log(msg.name);
                if ( typeof(callbacks[msg.name]) == "function" ){
                    callbacks[msg.name](e);
                }
            } catch (exc) {
                console.log(["problem invoking callback for ", e, exc, callbacks])
            };
        },
        function (origin) {
            return true;
        }
    ); // support for different domains
});