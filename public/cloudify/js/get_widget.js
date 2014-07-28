function cloudifyWidgetInitialization(){
    function cloudifyWidgetOnLoadHandler(){
        var element = document.getElementById("cloudify-widget");
        var api_key = element.getAttribute("data-api-key");
        var host = element.getAttribute("data-host") || "launch.cloudifysource.org";  // backward compatibility

        var iframe = document.createElement("iframe");
        element.parentNode.insertBefore(iframe, element.nextSibling);

        // todo : make protocol insensitive once we support HTTPS.
        // http://localhost:9000/public/angularApps/index.html#/widgets/4a7c12a1-3e1b-412d-b674-8329480f93fd/view?since=1406540993919
        iframe.setAttribute("src", "http://" + host + "/public/angularApps/index.html#/widgets/" + api_key + '/view?since=' + new Date().getTime());
    }
    window.addEventListener("load", cloudifyWidgetOnLoadHandler, false);
}
cloudifyWidgetInitialization();
