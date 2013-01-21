function cloudifyWidgetInitialization(){
    function cloudifyWidgetOnLoadHandler(){
        var element = document.getElementById("cloudify-widget");
        var api_key = element.dataset["apiKey"];
        var host = element.dataset["host"] || "launch.cloudifysource.org";  // backward compatibility
        var title = element.dataset["title"];
        var video_url = element.dataset["video_url"];

        var params = ["apiKey=" + api_key, "title=" + title, "origin_page_url=" + window.location.href, "video_url=" + (video_url || "")];

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
