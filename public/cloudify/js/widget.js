$(function () {


    function is_requires_login(){
        return $("body").is("[data-requires-login]");
    }
  function get_params() {

      debugger;
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
    debugger;
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
        this.customLink = generateFieldFunction("publicIp");

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
                if ( $.isEmptyObject( currentCookie ) ) {
                    console.log( ["cookie is not removed for some reason, using logic delete instead"] )
                }
            }
        }
    };



    var widgetState = new WidgetState();

  function write_log(message, class_name) {
    $("#log").append($("<li/>", {html: message}).addClass(class_name));
    $("#log").scrollTop($("#log")[0].scrollHeight);
  }

    function setTimeoutForUpdateStatus( myTimeout )
    {
        myTimeout = ( typeof(myTimeout) == "undefined" || myTimeout == null ) ? 10000 : myTimeout;
        setTimeout( update_status, myTimeout );
    }

    function handleUpdateStatusSuccess( data )
    {
        if ( data.status.state == "stopped" ) {
            $( "#start_btn,#stop_btn" ).toggle();
            stop_instance();
        } else if ( data.status.state == "error" ) {
            $( "#start_btn,#stop_btn" ).toggle();
            write_log( data.status.message, "error" );
            stop_instance();
        } else {
            if ( window.log != data.status.output ) {
                $( "#log" ).empty();
                $.each( data.status.output, function ( index, value )
                {
                    write_log( value );
                } );
                window.log = data.status.output;
            }
            $( "#time_left_counter" ).text( data.status.timeleft + " minutes" );
            setTimeoutForUpdateStatus();
        }
    }

  function update_status() {
    $.ajax({
        type:'get',
        url: "/widget/"+ widgetState.instanceId() + "/status?apiKey=" + apiKey,
        success:function( data ){ handleUpdateStatusSuccess(data); },
        error:function(){ setTimeoutForUpdateStatus() }
    });
  }



  function stop_instance() {
    if( $("#log" ).find(".successfully_completed_msg" ).length == 0 ){ // make sure this appears only once.. we might be firing an Ajax request after the first stop.
        write_log("Test drive successfully completed! <br/><a class='download_link successfully_completed_msg' target='_blank' href='http://www.cloudifysource.org/downloads/get_cloudify'>Download Cloudify here</a> or read the <a class='documentation_link' target='_blank' href='http://www.cloudifysource.org/guide/2.3/qsg/quick_start_guide_helloworld'>documentation</a>.", "important");
    }
    $("#time_left, #links").hide();
    widgetState.remove( true );

  }

    function start_instance_btn_handler()
    {
        var myUrl = document.location.origin;
        console.log(["sending message", myUrl, parent ] );
        $.postMessage( JSON.stringify({name:"playwidget", comment:"requires_login"}), myUrl , parent );
        $.receiveMessage( function(e){
            var msg = JSON.stringify(e.data);
            if ( msg.name == "userlogin"){
                params["userId"] = e.data;
            }
        }, origin_page_url);
        console.log("after message");
        if ( is_requires_login() && !params["userId"] ){
            return;
        }

        $( "#start_btn,#stop_btn" ).toggle();
        if ( !widgetState.isValid() ) {
            $.post( "/widget/start?" + $.param( params ), {}, function ( data, textStatus, jqXHR )
            {
                if ( data.status == "error" ) {
                    $( "#start_btn,#stop_btn" ).toggle();
                    write_log( data.message, "error" );
                    return;
                }

                if ( data.instance["@instanceId"] ) {
                    widgetState.instanceId( data.instance["@instanceId"] ).publicIp( data.instance["@publicIP"] ).remove( false );
                    $( "#time_left" ).show();
                    setTimeoutForUpdateStatus( 1 );

                    var link_info = data.instance.link;
                    var custom_link = "<li id='custom_link'><a href='" + link_info.url + "' target='_blank'>" + link_info.title + "</a></li>";
                    set_cloudify_dashboard_link( custom_link );
                }
            } );
        }
    }

    function stop_instance_btn_handler() {
    if (!confirm("Are you sure you want to stop the instance?")) {
      return;
    }
    $("#start_btn,#stop_btn").toggle();
    if ( widgetState.instanceId()) {
      $.post("/widget/"+ widgetState.instanceId() + "/stop?apiKey=" + params["apiKey"], {}, function (data) {
        if (data.status == "error") {
          $("#start_btn,#stop_btn").toggle();
          write_log(data.message, "error");
          return;
        }
        stop_instance();
      });
    }
  }





  function set_cloudify_dashboard_link(custom_link) {
    $("#links").show();
    $("#cloudify_dashboard_link").attr("href", "http://" + widgetState.publicIp() + ":8099/");
      widgetState.customLink( custom_link );
    if ($("#custom_link").get(0))
      $("#custom_link").replaceWith(custom_link);
    else
      $("#links").append($(custom_link));
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
    $("#start_btn,#stop_btn,#time_left").toggle();
    set_cloudify_dashboard_link( widgetState.customLink() );
    setTimeoutForUpdateStatus( 1 );
  }

  $("#start_btn").click(start_instance_btn_handler);
  $("#stop_btn").click(stop_instance_btn_handler);

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
