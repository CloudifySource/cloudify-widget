// a general file for demo javascript
$(function () {

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