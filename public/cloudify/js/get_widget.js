function cloudifyWidgetInitialization(){
    function cloudifyWidgetOnLoadHandler(){
        var element = document.getElementById("cloudify-widget");
        var show_advanced = element.getAttribute("data-show-advanced");
        var api_key = element.getAttribute("data-api-key");
        var host = element.getAttribute("data-host") || "launch.cloudifysource.org";  // backward compatibility
        var title = element.getAttribute("data-title");
        var video_url = element.getAttribute("data-video_url");

        var paramsObj = {
            "apiKey" : api_key,
            "showAdvanced" : show_advanced || "true",
            "title":title,
            "origin_page_url": window.location.href,
            "video_url":video_url || ""

        };

        var params = [];
        for ( var i in paramsObj ){
            if ( paramsObj.hasOwnProperty(i)){
                params.push(i + "=" + encodeURIComponent(paramsObj[i]));
            }
        }

        var iframe = document.createElement("iframe");
        element.parentNode.insertBefore(iframe, element.nextSibling);

        // todo : make protocol insensitive once we support HTTPS.
        iframe.setAttribute("src", "http://" + host + "/widget/widget?" + params.join("&"));
        iframe.setAttribute("width", "600px");
        iframe.setAttribute("height", "463px");
        iframe.setAttribute("frameborder", "no");
    }
    window.addEventListener("load", cloudifyWidgetOnLoadHandler, false);
}
cloudifyWidgetInitialization();
