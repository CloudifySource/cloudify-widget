// a general file for demo javascript
$(function () {

    function get_params() {

        var params = {};
        var hash;
        var hashes = window.location.search.substring(1).split('&');
        console.log(["using search tearm", hashes ]);
        for(var i = 0; i < hashes.length; i++)
        {
            hash = hashes[i].split('=');
            if ($.trim(hash[1]) != ""){
                params[hash[0]] = hash[1];
            }
        }
        console.log(["params are",params]);
        return params;
    }

    var params = get_params();
    var origin_page_url = params["origin_page_url"];


    var msgSource = $("#cloudify-widget").attr("src").split("/js")[0];
    console.log("registering event for " + msgSource);
    var callbacks = {};
    callbacks["require_login"] = function (e) {
        $("body").trigger("requireLogin");
    };
//    callbacks["instance_available"] = function(e) {};
    callbacks["widget_status"] = function(e) {
        var status = JSON.parse(e.data).status;
        console.dir(status);
        status.cloudifyUiIsAvailable && $.postMessage( JSON.stringify({name:"cloudify_ui_available"}), origin_page_url , parent );
//        status.instanceIsAvailable
//        status.consoleLink
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