$(function () {



    function advancedData( ){

        var cookieName = "ADVANCED_DATA";
        var initialized = false;

        var data = {"project" : null, "key":null, "secretKey":null};

        function getterSetter( key ){
            return function(_value){
                if ( typeof(_value)== "undefined"){
                    return data[key];
                }
                data[key] = _value;
            }
        }

        this.project = getterSetter("project");
        this.key = getterSetter("key");
        this.secretKey = getterSetter("secretKey");


        function _isEmpty(){
            for ( var v in data ){ if ( data[v] == null) return true;} return false;
        }

        function dataToSave(){
            return _isEmpty() ? null : JSON.stringify( data );
        }

        function _save( data ){
            var date = new Date();
            date.setMonth( ( date.getMonth() + 1 ) % 12 );
            $.cookie(cookieName, data, {"path" :"/", expires:date });
        }

        this.prolong = function(){
            var rawVal = $.cookie(cookieName);
            _save(rawVal);
        }

        this.save = function(){

            $.ajax({
                url: "/encrypt?data=" + encodeURIComponent(dataToSave()),
                async: false,
                success: function(result) {
                    _save(result);
                }
            });
        };

        this.clear = function(){
            for ( var v in data ){
                data[v] = null;
            }
        };

        this.read = function(){
            initialized = true;
            var v = $.cookie(cookieName);
            try{
                if ( v != null && typeof(v) == "string" ){
                    var that = this;
                    $.ajax({
                        url: "/decrypt?data=" + encodeURIComponent(v),
                        async: false,
                        success: function(result) {
                            data = JSON.parse(result);
                            return that;
                        }
                    });
                }
            }catch(e){
                console.log(["unable to read advanced data",e])
            };
            return this;
        };


        this.isEmpty = function(){
            return _isEmpty();
        } ;

        // either initialized already, and not empty
        // or - lets initialize and check not empty
        this.exists = function(){
            return ( initialized && this.isEmpty() ) || !(this.read().isEmpty())
        }
    }

    var advancedCookie = new advancedData();

    function advanced( project, key, secretKey ){

        var $advanced = $(".advanced_section");
        var $project = $advanced.find("[name=project_name]");
        var $key = $advanced.find("[name=hpcs_key]");
        var $secretKey = $advanced.find("[name=hpcs_secret_key]");
        if ( project && key && secretKey )
        {
            $project.val(project);
            $key.val(key);
            $secretKey.val(secretKey);
        }else{
            return { project : $project.val(), key : $key.val(), secretKey : $secretKey.val() }
        }
    }

    function populateProjectNameDataList(){
        // load into datalist
        if ( advancedCookie.exists()){
            var $datalist = $("datalist#advancedProject option");
            if ( $datalist.length >  0){
                $datalist.attr("value", advancedCookie.project());
            }else{ // need to create the data list
                $("<datalist>", {"id":"advancedProject"} ).append($("<option/>", {"value":advancedCookie.project()})).appendTo("body");
                $(".advanced_section [name=project_name]" ).attr("list","advancedProject");
            }
            $("body" ).trigger("updateDataList");
        }
    }

    populateProjectNameDataList(); // on page load

    // only load datalist polyfill scripts if datalist not supported by the browser
    Modernizr.load({
        test: Modernizr.datalistelem,
        nope: ['/public/js/jquery.relevant-dropdown.js', '/public/js/load.datalist.js']
    });


    $(".advanced_section [name=project_name]" ).bind('change blur',function(){
        var v = $(this ).val();

        if ( advancedCookie.project() == v ){
            advanced(v, advancedCookie.key(), advancedCookie.secretKey());
        }
    });

    function show_walkthrough(){

        var polling_ptr = null;

        function show_walkthrough(){
            $("#walkthrough" ).show();
        }

        function dismiss(){
            $.cookie("walkthrough", "walkthrough", {"path":"/"});

            $("#walkthrough" ).hide();
        }

        function poll_is_dismissed(){
           clearInterval(polling_ptr);
        }

        function is_dismissed(){
            return $.cookie("walkthrough", {path:"/"}) != null;
        }

        setInterval(poll_is_dismissed,1000);

    }


    function is_requires_login(){
        return $("body").is("[data-requires-login]");
    }
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
  var origin_page_host = origin_page_url.replace( /([^:]+:\/\/[^\/]+).*/, '$1' );


    // guy - we should consider using $.cookie.json=true
    // it will save us the trouble of parsing back and forward the cookie value
    // read more at https://github.com/carhartl/jquery-cookie
    var WidgetState = function(){
        var widgetIsPlaying = false;
        var cookieName = "widgetCookie" + origin_page_url;
        function _get(){
            try{
                var cookieValue = JSON.parse( $.cookie(cookieName) );
                return cookieValue == null ? {} : cookieValue; // never return null
            }catch(e){ console.log(["error parsing cookie. deleting the cookie ",e]);$.cookie( cookieName,null, {'path':'/'} ); return {}; }
        }

        function _set( obj ){
            var newObj = $.extend(_get(), obj);
            $.cookie( cookieName, JSON.stringify(newObj), { 'path' : '/' } );
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
                    $.cookie( cookieName, null, {'path':'/'} );
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
            if ( !handler ){
                $("#stop_btn").click();
            }else{
                $("#stop_btn").click( function(){  handler(arguments);});
            }
        };
        this.onPlay = function( handler ){
            if ( !handler ){
                $("#start_btn").click();
            }else{
                $("#start_btn").click( function(){ handler(arguments);});
            }
        };
        this.isPlaying = function( value ){
            if ( typeof(value) == "undefined"){
                return widgetIsPlaying;
            }
            else{
                widgetIsPlaying = value;
            }
        }
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




        var ellipsis = ".....";
        var ellipsis_iteration = 0;
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
            write_log(ellipsis.substring( 0, (ellipsis_iteration++ % 5) + 1),"log-ellipsis" );
        };

        this.clear= function(){clear()};
        this.error = function (message){ write_log(message, "error"); };
        this.important = function(message){ write_log(message,"important");};
    };



    var widgetState = new WidgetState();
    var widgetLog = new WidgetLog();

    function setTimeoutForUpdateStatus( myTimeout )
    {
        myTimeout = ( typeof(myTimeout) == "undefined" || myTimeout == null ) ? 3000 : myTimeout;
        setTimeout( update_status, myTimeout );
    }


    function get_custom_link(  ){
        var link_info = widgetState.customLink();
        return $("<li></li>", {"id":"custom_link", "class":"mock"}).append($("<span></span>", {"text":link_info.title, "class":"mock_text"})).append($("<a></a>", {"href": link_info.url, "target": "_blank", "text": link_info.title}));
    }


    $(".remember_creds" ).on( {
        "yes":function(){
            var ad = advanced();
            console.log("saving advanced");
            advancedCookie.project(ad.project);
            advancedCookie.key(ad.key);
            advancedCookie.secretKey(ad.secretKey);
            advancedCookie.save();


            populateProjectNameDataList();


            $(this ).hide();
        },
        "no":function(){
            console.log("not saving advanced");
            $(this ).hide();
        }
    });


    function handleUpdateStatusSuccess( data )
    {
        $.postMessage( JSON.stringify({name:"widget_status", comment:"status_was_updated", status:data.status}), origin_page_url , parent );
        if ( data.status.timeleft ) {
            $("#time_left").show();
            $("#time_left_counter").text(data.status.timeleft + " minutes");
        }

        if ( data.status.output ){
            widgetLog.appendOrOverride(data.status.output);
        }

        if ( data.status.remote ){
            $("#hp_console_link" ).show();
        }

        if ( data.status.publicIp ) {
            widgetState.publicIp(data.status.publicIp);
        }

        if ( data.status.hasPemFile && $("#pemFileLink").is(":not(:visible)") ){
            $("#pemFileLink").attr("href", jsRoutes.controllers.Application.downloadPemFile(widgetState.instanceId()).url).show();
        }

        if ( data.status.cloudifyUiIsAvailable ){ // must be after publicIp
            show_cloudify_ui_link( data.status.cloudifyUiIsAvailable );
        }

        if ( data.status.instanceIsAvailable ){
            console.log(["installation finished", data]);
        }

        if ( data.status.consoleLink ) {
            var link_info = data.status.consoleLink;
            widgetState.customLink( link_info );
            show_custom_link( data.status.instanceIsAvailable );
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
        widgetState.isPlaying(false);

    }

    console.log(["registering event on " , origin_page_url ]);
    $.receiveMessage( function(e){
        console.log(["widget got a message",e]);
        var msg = JSON.parse(e.data);
        if ( msg.name == "user_login"){
            params["userId"] = msg.userId;
        } else if ( msg.name == "play_widget"){
            if ( widgetState.isPlaying() ){ // this is an echo.
                return;
            }
            if ( msg.advanced ){
                advanced( msg.advanced.project, msg.advanced.key , msg.advanced.secretKey);
            }else{
                advanced("","","");
            }
           widgetState.onPlay();
        } else if ( msg.name == "stop_widget"){
            if ( !widgetState.isPlaying() ){ // might be an echo.
                return;
            }
            widgetState.onStop();
        }
     }, origin_page_host ); // origin_page_host


    function isEmpty( str ){
        return str == null || $.trim(str ).length == 0;
    }

    function advancedDataAvailable( advancedData ){
        return   !isEmpty(advancedData.project)  && !isEmpty(advancedData.key) && !isEmpty(advancedData.secretKey)
    }

    function start_instance_btn_handler()
    {
        var myUrl = origin_page_url;
        console.log(["sending message", myUrl, parent ] );
        console.log("after message");
        if ( is_requires_login() && !params["userId"] ){
            $.postMessage( JSON.stringify({name:"require_login"}), myUrl , parent );
            return;
        }else{
            $.postMessage( JSON.stringify({name:"play_widget"}), myUrl , parent );
        }
        widgetState.isPlaying(true);



        try{

            mixpanel.track("Play Widget",{'page name' : $("#title" ).text(), 'url' : origin_page_url, "anonymous" : !advancedDataAvailable( advanced() ) });
        }catch(e){}
        widgetState.showStopButton();
        if ( !widgetState.isValid() ) {
            var advancedData = advanced();
			var playData = { apiKey : params["apiKey"], "project" : advancedData.project, "key" : advancedData.key, "secretKey":advancedData.secretKey };
            widgetLog.appendOrOverride(["Acquiring machine..."]);
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

                        if ( advancedDataAvailable( advancedData ) ){
                            if (advancedData.project == advancedCookie.project() &&
                                advancedData.key == advancedCookie.key() &&
                                advancedData.secretKey == advancedCookie.secretKey()) {

                                advancedCookie.prolong();

                            } else {
                                $(".remember_creds").show();
                            }
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
        $.postMessage( JSON.stringify({name:"stop_widget"}), origin_page_url , parent );
    widgetState.showPlayButton();
    if ( widgetState.instanceId()) {
      $.post("/widget/"+ widgetState.instanceId() + "/stop?apiKey=" + params["apiKey"], {}, function (data) {
        if (data && data.status && data.status.state == "error") {
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
        $("#links").find("li:first").removeClass("mock");
        $("#cloudify_dashboard_link").attr("href", "http://" + widgetState.publicIp() + ":8099/");
      }else{
          $("#links").find("li:first").addClass("mock");
      }
  }


    function show_custom_link( enabled ) {
        if ( enabled ) {
            var custom_link = get_custom_link();
            if ( $( "#custom_link" ).get( 0 ) ) {
                $( "#custom_link" ).replaceWith( $( custom_link ) );
            }
            else {
                $( "#links" ).append( $( custom_link ) );
            }
          $("#custom_link" ).removeClass("mock");
        }else{
            $("#custom_link" ).addClass("mock");
        }
    }


    $("#title").text(decodeURIComponent(params["title"]));

    var apiKey = params["apiKey"];
    var shareUrl= encodeURI("http://launch.cloudifysource.org/admin/signin");

    var msg =  encodeURI("Launch on the cloud in a single click using the Cloudify widget");
    var twitterMsg = encodeURIComponent( $("#title" ).text() + " on any cloud with a single click with #cloudifysource");

    var $embedCodeBox = $("#embed-code-box").hide();
    // remove white spaces
    $embedCodeBox.val($.trim($embedCodeBox.val()));
    // tie show/hide handlers
    $("#embed_btn").click(function() {
        $embedCodeBox.toggle();
    });
    $("#embed-code-box .close").click(function() {
        $embedCodeBox.hide();
    });

    $("#facebook_share_link").attr("href", "http://www.facebook.com/sharer/sharer.php?u=" + shareUrl);
    $("#google_plus_share_link").attr("href", "https://plus.google.com/share?url=" + shareUrl);
    $("#twitter_share_link").attr("href", "https://twitter.com/share?url=" + encodeURIComponent(origin_page_url) + "&text=" + twitterMsg);

    // select all on focus/click for autoselect textareas
    $("textarea.autoselect").focus(function() {
        var $this = $(this);

        $this.select();

        window.setTimeout(function() {
            $this.select();
        }, 1);

        // Work around WebKit's little problem
        $this.mouseup(function() {
            // Prevent further mouseup intervention
            $this.unbind("mouseup");
            return false;
        });
    });


    // moved to template instead.
//  if (params["video_url"]) {
////    $("#video_container").append($("<iframe id='youtube_iframe' width='270' height='160' wmode='transparent' frameborder='0' allowfullscreen></iframe>"));
////    $("#youtube_iframe").attr("src", decodeURIComponent(params["video_url"]));
//
////      var videoUrl =  decodeURIComponent(params["video_url"]);
////      var utube = '<object width="420" height="315"><param name="movie" value="' + videoUrl + '?version=3&amp;hl=en_US"></param><param name="allowFullScreen" value="true"></param><param name="allowscriptaccess" value="always"></param><embed src="' + videoUrl + '?version=3&amp;hl=en_US" type="application/x-shockwave-flash" width="420" height="315" allowscriptaccess="always" allowfullscreen="true"></embed></object>'
////      $("#video_container" ).append($(utube));
//  }

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

        $(".download_link" ).live("click",function(){
            mixpanel.track("Download Button Clicks",{'page name' : $("#title" ).text(), 'url' : origin_page_url});
        });

        $(".documentation_link" ).live("click",function(){
            mixpanel.track("Documentation Button Clicks",{'page name' : $("#title" ).text(), 'url' : origin_page_url});
        });

   mixpanel.track("Widget Impression", {"widgetTitle" : $("#title" ).text()});

  $(".share_link").click(function (e) {
    e.preventDefault();
    var leftvar = (screen.width-600)/2;
    var topvar = (screen.height-600)/2;

    mixpanel.track("Social Button Clicks",{'page name' : $("#title" ).text(), 'url' : origin_page_url, 'button': $(this ).attr("id")});

    window.open($(e.target).parents("a").attr("href"), '', 'menubar=no,toolbar=no,resizable=yes,scrollbars=yes,height=600,width=600,left='+ leftvar +',top=' + topvar);

  });

    $("form.advanced_form" ).submit(function(e){
        e.stopPropagation();
       console.log("submitting form");
        widgetState.onPlay();
        return false;
    });

    // code for walkthrough on the widget end.
//    var checkWTInterval = null;
//    // handle walkthrough
//    function shouldShowWalkthrough(){
//           return $.cookie("dismissWT") != "true";
//       }
//
//    function hideWT(){
//        $.cookie("dismissWT",true, {path:"/"});
//        console.log("hiding walkthrough");
//        $("#walkthrough" ).remove();
//        if ( checkWTInterval != null ){
//            clearInterval(checkWTInterval);
//        }
//    }
//       function checkWalkthrough(){
//           if ( !shouldShowWalkthrough() ){
//               hideWT();
//           }
//       }
//
//       if ( shouldShowWalkthrough() ){
//           $("#walkthrough" ).show();
//           $("#walkthrough" ).click(function(){
//               hideWT();
//
//               });
//           checkWTInterval = setInterval( checkWalkthrough , 1000 );
//       } else{
//           hideWT();
//       }


});
