$(function () {



  function get_params() {
    var params = [], hash;
    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for(var i = 0; i < hashes.length; i++)
    {
      hash = hashes[i].split('=');
      params.push(hash[0]);
      params[hash[0]] = hash[1];
    }
    return params;
  }

  var params = get_params();
  var origin_page_url = params["origin_page_url"];


    var WidgetState = function(){
        var cookieName = "widgetCookie" + origin_page_url;
        function _get(){
            try{
                var cookieValue = JSON.parse( $.cookie(cookieName) );
                return cookieValue == null ? {} : cookieValue; // never return null
            }catch(e){ console.log(["error parsing cookie. deleting the cookie ",e]);$.cookie( cookieName,null ); return {}; }
        }

        function _set( obj ){
            var newObj = $.extend(_get(), obj);
            $.cookie( cookieName, JSON.stringify(newObj) );
        }

        function get_or_set( key, val )
        {
            if ( !val || typeof(val) == "undefined" ) {
                return _get()[key];
            } else {
                var toSet={};
                toSet[key]= val;
                _set( toSet );
                return this;
            }
        }


        // should make sure cookie is not empty and not removed ( we support logical remove )
        this.isValid = function ()
        {
            var currentCookie = _get();
            return currentCookie != null && typeof(currentCookie) != "undefined" && !$.isEmptyObject( currentCookie ) && !currentCookie.removed
        };


        function generateFieldFunction( key ){ return function( val ){ return get_or_set.call(this,key,val) };}
        this.instanceId = generateFieldFunction("instanceId");
        this.publicIp = generateFieldFunction("publicIp");
        this.customLink = generateFieldFunction("customLink");

        this.remove = function ( remove )
        {
            _set( { removed: remove } ); // logic remove just in case removing the cookie does not work
            if ( remove ) {  // try removing the cookie. see issue #40

                try {
                    $.removeCookie( cookieName );
                    $.cookie( cookieName, null );
                } catch ( e ) {
                    console.log( ["error removing cookie", e] )
                }
                var currentCookie = _get();
                if ( !$.isEmptyObject( currentCookie ) ) {
                    console.log( ["cookie is not removed for some reason, using logic delete instead"] )
                }
            }
        };

        // guy - todo - replace this with a CSS solution on the widget div.
        // guy - todo - replace the "click" events with "start_widget" and "stop_widget" events and listen to the widget.
        // guy - todo - go over this script, make sure everything relates to the widget. Widget.log, Widget.start, Widget.stop etc..
                        // show time left
                        // set custom link
                        // getRemoteCredentials
                        // etc..
                        // get rid of ID references outside the object.
        this.showStopButton = function(){
            $("#stop_btn").show();
            $("#play_btn").hide();
        };
        this.showPlayButton = function(){
            $("#stop_btn").hide();
            $("#play_btn").show();
        };
        this.onStop = function( handler ){
            $("#stop_btn").click(stop_instance_btn_handler);
        };
        this.onPlay = function( handler ){
            $("#start_btn").click(start_instance_btn_handler);
        };
    };

    var WidgetLog = function(){
        var myLog = [];
        var $dom = $("#log");

        function clear(){
            $dom.empty();
        }

        function write_log(message, class_name) {
          $dom.append($("<li/>", {html: message}).addClass(class_name));
          $dom.scrollTop($dom[0].scrollHeight);
        }

        // array of output strings.
        // this function will append new lines of log if new log is longer than current log.
        // otherwise it will clear the current log and rewrite it.
        this.appendOrOverride = function (aOutput) {
            if (myLog.length!= aOutput.length || myLog[0] != aOutput[0]) { // print only the difference

                var index = myLog.length;
                var logLength = aOutput.length;

                if (logLength <= index) {
                    clear();
                    index = 0;
                }

                for (; index < logLength; index = index + 1) {
                    write_log(aOutput[index]);
                }
                myLog = aOutput;
            }
        };

        this.clear= function(){clear()};
        this.error = function (message){ write_log(message, "error"); };
        this.important = function(message){ write_log(message,"important");};
    };



    var widgetState = new WidgetState();
    var widgetLog = new WidgetLog();

    function setTimeoutForUpdateStatus( myTimeout )
    {
        myTimeout = ( typeof(myTimeout) == "undefined" || myTimeout == null ) ? 10000 : myTimeout;
        setTimeout( update_status, myTimeout );
    }


    function get_custom_link(  ){
        var link_info = widgetState.customLink();
        return $("<li></li>", {"id":"custom_link"}).append($("<a></a>", {"href": link_info.url, "target": "_blank", "text": link_info.title}));
    }

    function handleUpdateStatusSuccess( data )
    {

        if ( data.status.timeleft ) {
            $("#time_left").show();
            $("#time_left_counter").text(data.status.timeleft + " minutes");
        }

        if ( data.status.output ){
            widgetLog.appendOrOverride(data.status.output);
        }

        if ( data.status.publicIp ) {
            widgetState.publicIp(data.status.publicIp);
        }

        if ( data.status.cloudifyUiIsAvailable ){ // must be after publicIp
            show_cloudify_ui_link( data.status.cloudifyUiIsAvailable );
        }

        if ( data.status.instanceIsAvailable ){
            console.log(["installation finished", data]);
            if (data.status.consoleLink) {
                var link_info = data.status.consoleLink;
                widgetState.customLink( link_info );
                show_custom_link(true);
            }
       }


        var state = data.status.state.toLocaleLowerCase();
        if ( state == "stopped") {
            widgetState.showPlayButton();
            stop_instance( data.status.message );
        } else {// status == running
            setTimeoutForUpdateStatus();
        }
    }

  function update_status() {
      if ( widgetState.instanceId() ){  // guy - double check. saw that sometimes it misses.
            $.ajax({
                type:'get',
                url: "/widget/"+ widgetState.instanceId() + "/status?apiKey=" + apiKey,
                success:function( data ){ handleUpdateStatusSuccess(data); },
                error:function(){ setTimeoutForUpdateStatus() }
            });
      }else{
          stop_instance();
      }
  }


    function stop_instance(msg) {
        if ( msg ){
            widgetLog.error(msg);
        }
        else if ($("#log").find(".successfully_completed_msg").length == 0) { // make sure this appears only once.. we might be firing an Ajax request after the first stop.
            widgetLog.important("Test drive successfully completed! <br/><a class='download_link successfully_completed_msg' target='_blank' href='http://www.cloudifysource.org/downloads/get_cloudify'>Download Cloudify here</a> or read the <a class='documentation_link' target='_blank' href='http://www.cloudifysource.org/guide/2.3/qsg/quick_start_guide_helloworld'>documentation</a>.");
        }
        show_cloudify_ui_link(false);
        show_custom_link(false);
        $("#time_left").hide();
        widgetState.remove(true);

    }

    function start_instance_btn_handler()
    {
        widgetState.showStopButton();

        if ( !widgetState.isValid() ) {
			var playData = { apiKey : params["apiKey"], "hpcsKey" : $("#advanced [name=hpcs_key]").val(), "hpcsSecretKey":$("#advanced [name=hpcs_secret_key]").val() };
            $.ajax(
                { type:"POST",
                  url : "/widget/start?" + $.param(playData),
                    success: function (data, textStatus, jqXHR) {
                        var state = data.status.state.toLowerCase();
                        if (state == "error" || state == "stopped") {
                            widgetState.showPlayButton();
                            widgetLog.error(data.status.message);
                            return;
                        }
                        if (data.status.instanceId) {
                            widgetState.instanceId(data.status.instanceId);
                        }

                        setTimeoutForUpdateStatus(1);
                    },
                    error: function (data) {
                        var displayMessage = data.getResponseHeader("display-message");
                        if (displayMessage) {
                            var displayMessageObj = JSON.parse(displayMessage);
                            widgetLog.error(displayMessageObj.msg);
                        }
                    }
                }

            );
        }
    }

    function stop_instance_btn_handler() {
    if (!confirm("Are you sure you want to stop the instance?")) {
      return;
    }
    widgetState.showPlayButton();
    if ( widgetState.instanceId()) {
      $.post("/widget/"+ widgetState.instanceId() + "/stop?apiKey=" + params["apiKey"], {}, function (data) {
        if (data.status.state == "error") {
          widgetState.showPlayButton();
          widgetLog.error(data.status.message );
          return;
        }
        stop_instance();
      });
    }
  }


  function show_cloudify_ui_link( show ){
      if ( show ){
        $("#links").find("li").show();
        $("#cloudify_dashboard_link").attr("href", "http://" + widgetState.publicIp() + ":8099/");
      }else{
          $("#links").find("li").hide();
      }
  }


    function show_custom_link( show ) {
        if ( show ){
            var custom_link = get_custom_link();
            if ($("#custom_link").get(0)){
                $("#custom_link").replaceWith($(custom_link));
            }
            else{
                $("#links").append($(custom_link));
            }
        }else{
            $("#custom_link").remove();
        }
    }


    $("#title").text(decodeURIComponent(params["title"]));

  var apiKey = params["apiKey"];
  var shareUrl= encodeURI("http://launch.cloudifysource.org/admin/signin");

  var msg =  encodeURI("Launch on the cloud in a single click using the Cloudify widget");
    var twitterMsg = encodeURIComponent( $("#title" ).text() + " on any cloud with a single click with #cloudifysource");
  $("#facebook_share_link").attr("href", "http://www.facebook.com/sharer/sharer.php?u=" + shareUrl);
  $("#google_plus_share_link").attr("href", "https://plus.google.com/share?url=" + shareUrl);
  $("#twitter_share_link").attr("href", "https://twitter.com/share?url=" + encodeURIComponent(origin_page_url) + "&text=" + twitterMsg);



  if (params["video_url"]) {
    $("#video_container").append($("<iframe id='youtube_iframe' width='270' height='160' frameborder='0' allowfullscreen></iframe>"));
    $("#youtube_iframe").attr("src", decodeURIComponent(params["video_url"]));
  }

  if (widgetState.instanceId()) {
        widgetState.showStopButton();

      // lets update these just in case the update status fails.
      if (widgetState.publicIp()) {
          show_cloudify_ui_link( true );
      }

      if (widgetState.customLink()) {
          show_custom_link( true );
      }
    setTimeoutForUpdateStatus( 1 );
  }


    widgetState.onPlay( start_instance_btn_handler );
    widgetState.onStop( stop_instance_btn_handler );


  $("#show_advanced, #hide_advanced").click(function () {
    $("#advanced").toggle();
  });


        $(".download_link" ).live("click",function(){
            mixpanel.track("Download Button Clicks",{'page name' : $("#title" ).text(), 'url' : origin_page_url});
        });

        $(".documentation_link" ).live("click",function(){
            mixpanel.track("Documentation Button Clicks",{'page name' : $("#title" ).text(), 'url' : origin_page_url});
        });

        mixpanel.track("Widget Impression");

  $(".share_link").click(function (e) {
    e.preventDefault();
    var leftvar = (screen.width-600)/2;
    var topvar = (screen.height-600)/2;

    mixpanel.track("Social Button Clicks",{'page name' : $("#title" ).text(), 'url' : origin_page_url, 'button': $(this ).attr("id")});

    window.open($(e.target).parents("a").attr("href"), '', 'menubar=no,toolbar=no,resizable=yes,scrollbars=yes,height=600,width=600,left='+ leftvar +',top=' + topvar);

  });
});
