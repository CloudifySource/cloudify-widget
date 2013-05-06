// a general file for demo javascript
$(function () {
    var msgSource = $("#cloudify-widget").attr("src").split("/js")[0];
    console.log("registering event for " + msgSource);
    var callbacks = {};
    callbacks["requirelogin"] = function (e) {
        $("body").trigger("requireLogin");
    };
    /*callbacks["widgetstatus"] = function(e) {
        console.log('handling widgetstatus, status: ' + e.data.status);
    }*/
    $.receiveMessage(function (e) {
            console.log(["parent got the message", e]);
            try {
                var msg = JSON.parse(e.data);
                if ( typeof(callbacks[msg.name]) =="function" ){
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