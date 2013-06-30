jQuery.fn.reset = function ()
{
    $( this ).each( function ()
    {
        this.reset();
    } );
};

$( function ()
{
    if ($.cookie('newDashboard') != null ){
        document.location = "/user/widgets";
    }

    function WidgetModel(){
        var data = [];

        this.setWidgetList = function( widgetList ){
            data = widgetList;
        };

        this.getWidgetById = function( widgetId ){
            for ( var i = 0, j = data.length ; i < j; i += 1 ){
                if ( data[i]["id"] == widgetId ){
                    return data[i];
                }
            }
            return null;
        };
    }

    var widgetsModel =  new WidgetModel();
    // todo : ?? -- what is this method? --
    function remove_session()
    {
        $.removeCookie( "authToken" );
        $.removeCookie( "username" );
        $.removeCookie( "admin" );
        window.location.href = "/admin/signin";
    }

    function render_summary()
    {
        $( "#summary ul" ).empty();
        $.get( "/admin/pool/summary?authToken=" + authToken, {}, function ( data )
        {
            if ( data.status == "session-expired" ) {
                remove_session();
                return
            }

            $.each( data.summary.attributes, function ( index, attr )
            {
                $( "#summary ul" ).append( "<li><span class='count'>" + attr.value + "</span>" + attr.name + "</li>" );
            } );
        } );
    }


    function render_widget( widget )
    {
        var widget_tr = $( "#widget_record" ).tmpl( $.extend({
               admin:admin,
                host:window.location.host,
                instanceCount:  (widget.instances ? widget.instances.length : 0)
            },widget));



        if ( widget.instances ) {
            instance_search_cache[widget["id"]] = widget_tr.find( ".instance_search" ).quicksearch( "#widget_" + widget["id"] + "_instances tr" );
            var instances_container = widget_tr.find( "#widget_" + widget["id"] + "_instances" );

            $.each( widget.instances, function ( index, instance )
            {
                var instanceTmpl = $( "#widget_instance_tmpl" ).tmpl( instance );
                instances_container.append( instanceTmpl );
            } );
        }

        return widget_tr;
    }

    function parse_url( url )
    {
        var params = [], hash;
        var hashes = url.slice( url.indexOf( '?' ) + 1 ).split( '&' );
        for ( var i = 0; i < hashes.length; i++ ) {
            hash = hashes[i].split( '=' );
            params.push( hash[0] );
            params[hash[0]] = hash[1];
        }
        return params;
    }

    function youtube_parser( url )
    {
        if ( url.indexOf( "/embed/" ) == -1 ) {
            var video_id = parse_url( url )["v"];
            if ( !video_id )
                return null;
            return "http://www.youtube.com/embed/" + video_id;
        } else
            return url;
    }

    window.update_widget_list = function ( idToHighlight )
    {

        jsRoutes.controllers.WidgetAdmin.getAllWidgets( authToken ).ajax( {
            success: function ( data )
            {
                $( "#widget_list" ).empty();
                widgetsModel.setWidgetList(data);

                $("#main.widgets-dashboard" ).removeClass("empty-list");
                if ( data.length == 0 ) {
                    $("#main.widgets-dashboard" ).addClass("empty-list");
                    return;
                }

                $.each( data, function ( index, widget )
                {
                    var renderedWidget = render_widget( widget );
                    widget.domElement = renderedWidget;
                    renderedWidget.appendTo( "#widget_list" );
                    quicksearch.cache();
                    $.each( instance_search_cache, function ( i, instance_cace )
                    {
                        instance_cace.cache();
                    } );
                } );

                render_summary();
                if ( idToHighlight ){
                    $("[data-widget_id=" + idToHighlight +"] td" ).effect("highlight",{},1000);
                }
            }} );
    };

    window.authToken = $.cookie( "authToken" );
    window.username = $.cookie( "username" );
    window.admin = $.cookie( "admin" ) == "true";
    window.instance_search_cache = {};
    window.update_time_interval = 60000;

    // edit it to edit the new widget form tips
    var field_tips = {
        recipeName:"The name in the recipe for uninstall.",
        consoleUrlService:"The name of the service replacing $HOST placeholder",
        productName: "The name of your product",
        productVersion: "The version of your product",
        title: "The widget title as it will appear when displaying the widget within a web page",
        youtubeVideoUrl: "URL of a YouTube video you want to display within the widget (Optional)",
        providerURL: "The URL of the product owner, e.g. http://www.mongodb.org",
        recipeURL: "A URL (http/https)to the recipe zip file",
        consolename: "The title of the link to the product dashboard / UI in the widget console",
        consoleurl: "The URL to the product dashboard / UI. Use $HOST as the hostname placeholder, e.g.: http://$HOST:8080/tomcat/index.html"
    };

    if ( !authToken ) {
        window.location.href = "/admin/signin"; // todo : ??? -- login verification?? -- should be on server side.
    }

    $( "#username" ).text( username );
    if ( admin ) {
        $("#main.widgets-dashboard" ).addClass("admin-view");
    }

    for ( var key in field_tips ) {
        if ( field_tips.hasOwnProperty(key) && field_tips[key] ) {
            $( "#" + key ).after( $( "<i class='info-icon' id='" + key + "_info'></i>" ) );
            $( "#" + key + "_info" ).tooltip( {title: field_tips[key]} );
        }
    }

    window.quicksearch = $( 'input#search' ).quicksearch( "#widget_list tr" );
    window.update_interval = setInterval( update_widget_list, update_time_interval );
    update_widget_list();

    $( ".required_mark" ).attr( "title", "This field is mandatory" ).tooltip();

    function missingRequiredFields( form )
    {
        var error = false;
        $( form ).find( "input.required" ).each( function ( index, object )
        {
            if ( $( object ).val() == "" ) {
                error = true;
                $( object ).parents( ".control-group" ).addClass( "error" );
            }
        } );
        return error;
    }

    $( "form#change_password" ).submit( function ( e )
    {
        try {
            if ( missingRequiredFields( this ) ) {
                return false;
            }
            var $me = $( this );
            var data = $me.formParams();
            jsRoutes.controllers.WidgetAdmin.postChangePassword( authToken, data.oldPassword, data.newPassword, data.confirmPassword ).ajax( { complete: function ()
            {
                $me[0].reset()
            }} );
        } finally {
            e.stopPropagation();
            return false; // guy - this is ok, I always want to return false.
        }

    } );

    $("#edit_description_form" ).submit( function(e){

        try{
            var $me = $(this);

            var data = $me.formParams();
            var widget = widgetsModel.getWidgetById( data.widgetId );
            jsRoutes.controllers.WidgetAdmin.postWidgetDescription( authToken, data.widgetId, data.description ).ajax({

                form:this,
                success:function(){
                    $me[0].reset();
                    $me.closest(".modal" ).modal("hide");
                    widget.description = data.description;
                }

            });
        }finally{
            e.stopPropagation();
            return false;
        }

    });

    $("#require_login_form").submit(function (e) {
        try {
            var $me = $(this);
            var data = $me.formParams();
            var widget = widgetsModel.getWidgetById( data.widgetId );
            jsRoutes.controllers.WidgetAdmin.postRequireLogin(authToken, data.widgetId, data.requireLogin ? 1:0, data.loginVerificationUrl, data.webServiceKey).ajax({
                form:this,
                success: function () {
                    $me[0].reset();
                    $me.closest(".modal").modal('hide');
                    widget.loginVerificationUrl=data.loginVerificationUrl;
                    widget.webServiceKey = data.webServiceKey;
                    widget.requireLogin=data.requireLogin;
                }

            });
        } finally {
            e.stopPropagation();
            return false;
        }
    });

    $( "#new_widget_form" ).submit( function ( e )
    {
        e.preventDefault();

        var error = false;
        error = missingRequiredFields( this );

        var $youtubeVideoUrl = $( "#youtubeVideoUrl" );
        var youtube_video_url = $youtubeVideoUrl.val();
        if ( youtube_video_url ) {
            youtube_video_url = youtube_parser( youtube_video_url );
            if ( youtube_video_url ) {
                $youtubeVideoUrl.val( youtube_video_url );
                $youtubeVideoUrl.parents( ".control-group" ).removeClass( "error" );
            } else {
                error = true;
                $youtubeVideoUrl.parents( ".control-group" ).addClass( "error" );
                alert( "Youtube video url is incorrect" );
            }
        }

        if ( !error ) {
//            debugger;
            $.post( "/widget/new?authToken=" + encodeURIComponent(authToken) + "&" + $( e.target ).serialize(), {}, function ( data )
            {
                $( e.target ).reset();
                $( '#new_widget_modal' ).modal( 'hide' );
                update_widget_list( data.id );
            } );
        }
    } );



    $( ".modal-window" ).live( "show",function ()
    {
        console.log("clearing update interval");
        clearInterval( update_interval );
    } ).live( "hide", function ()
        {
            console.log("setting update interval");
            window.update_interval = setInterval( update_widget_list, update_time_interval ); // todo :??
        } );

    // reset Id value when closing the widget dialog.
    $("#new_widget_modal" ).live("hide", function(){ $(this ).find("#widgetId" ).val("")});

    function getWidgetByTarget( target ){
        return widgetsModel.getWidgetById( $(target).parents( "tr.widget ").attr("data-widget_id") )
    }

    function populateFormFromWidget( widget, $form ){
        $form.find(".controls [name]").each( function(index, item){
                    var $input = $(item);
                    if ( $input.attr("type") == "checkbox"){
                        $input.attr("checked", widget[$input.attr("name")] ? "checked":null);
                    }else{
                        $input.val( widget[$input.attr("name")]);
                    }

                });
        $form.find("[name=authToken]" ).val( authToken );

        $form.find("[name=widgetId]").val( widget.id );
    }

    $(".add_icon_btn btn-danger" ).click(function(e){
        $.ajax({

            url:'/widget/removeIcon?' + toUrlParams({"authToken":authToken, "widgetId":$(this ).closest("form" ).find("[name=widgetId]" ).val()}),
            success:function(){
                $("#file_upload_form_result" ).html("Icon removed successfully");
            }

        });

    });

    $(".add_icon_btn" ).live( "click", function(e){
        var widget = getWidgetByTarget( e.target );
        var $form = $("#add_icon_form");
        $("#file_upload_form_result" ).html("");
        populateFormFromWidget(widget, $form);
        $("#add_icon_modal" ).modal("show");

    });

    $(".edit_description_btn" ).live( "click", function(e){

        var widget = getWidgetByTarget( e.target );
        var $form = $("#edit_description_form");
        populateFormFromWidget(widget, $form);
        $("#edit_description_modal" ).modal("show");

    });

    $(".require_login_btn").live("click", function( e ) {
        var widget = getWidgetByTarget( e.target );
        var $form = $("#require_login_form");
         populateFormFromWidget(widget, $form);
        $("#require_login_modal").modal("show");
    });

    // copy contents of iframe after file upload
    $("#file_upload").load(function () {
       var iframeContents = $("#file_upload")[0].contentWindow.document.body.innerHTML;
       $("#file_upload_form_result").html(iframeContents);
    });


    $( ".edit_widget_btn" ).live( "click", function ( e )
    {
        var widgetId = $( e.target ).parents( "tr.widget" ).attr("data-widget_id");
        var widget = widgetsModel.getWidgetById( widgetId );

        var $form = $("#new_widget_form");
        $form.find(".controls [name]" ).each(function(index,item){
            var $input = $(item );
            $input.val( widget[$input.attr("name")] );
        });
        $form.find("[name=widgetId]" ).val( widget.id );

        $( '#new_widget_modal' ).modal( 'show' );

    } );

    $( ".disable_widget_btn" ).live( "click", function ( e )
    {
        var widget_container = $( e.target ).parents( "tr.widget" );
        var api_key = widget_container.data( "api_key" );
        if ( confirm( "Are you sure you want to disable widget " + api_key + "?" ) ) {
            // todo : we should probably use widget's ID to identify the widget. api_key is not as effective.
            $.post( "/widget/disable?authToken=" + authToken + "&apiKey=" + api_key, {}, function ( data )
            {
                if ( data.status == "session-expired" ) {
                    remove_session();
                    return
                }
                widget_container.removeClass("enabled-widget" ).addClass("disabled-widget");
            } );
        }
    } );

    function toUrlParams( data )
    {
        return $.param( data );
    }

    $( ".enable_widget_btn" ).live( "click", function ( e )
    {
        var widget_container = $( e.target ).parents( "tr.widget" );
        // todo : we should probably use widget's ID to identify the widget. api_key is not as effective.
        var postData = { apiKey: widget_container.data( "api_key" ), authToken: authToken };
        $.post( "/widget/enable?" + toUrlParams( postData ), {}, function ( data )
        {
            if ( data.status == "session-expired" ) {
                remove_session();
                return
            }
            widget_container.removeClass( "error" );
            widget_container.addClass("enabled-widget" ).removeClass("disabled-widget");

        } );
    } );

    $( ".delete_widget_btn" ).live( "click", function ( e )
    {
        if ( confirm( "Are you sure you want to delete this widget? This operation is not recoverable" ) ) {
            var $widget_container = $( e.target ).parents( "tr.widget" ); // guy - todo - I think we want closest here.. if have multiple tr.widget parent we will get faulty behavior.
            var postData = { apiKey: $widget_container.data( "api_key" ), authToken: authToken };
            // todo : we should probably use widget's ID to identify the widget. api_key is not as effective.
            jsRoutes.controllers.WidgetAdmin.deleteWidget( postData.authToken, postData.apiKey ).ajax( {
                success: function ( data )
                {
                    $widget_container.hide( 1000, function ()
                    {
                        $( this ).remove()
                    } );
                }
            } )
        }

    } );

    $( ".regenerate_key_btn" ).live( "click", function ( e )
    {
        var widget_container = $( e.target ).parents( "tr.widget" );
        var api_key = widget_container.data( "api_key" );
        if ( confirm( "Are you sure you want to regenerate api key for " + api_key + "?" ) ) {
            $.post( "/widget/regenerate?authToken=" + authToken + "&apiKey=" + api_key, {}, function ( data )
            {
                if ( data.status == "session-expired" ) {
                    remove_session();
                    return
                }
                $( "#widget_" + data.widget["id"] ).replaceWith( render_widget( data.widget ) );
                quicksearch.cache();
            } );
        }
    } );

    $( ".shutdown_instance" ).live( "click", function ( e )
    {
        var instance_container = $( e.target ).parents( "tr.instance" );
        var instance_id = instance_container.data( "instance_id" );
        if ( confirm( "Are you sure you want to shutdown the instance " + instance_id + "?" ) ) {
            $.post( "/widget/" + instance_id + "/shutdown?authToken=" + authToken, {}, function ( data )
            {
                if ( data.status == "session-expired" ) {
                    remove_session();
                    return
                }
                instance_container.fadeOut( 'fast', function ()
                {
                    instance_container.remove();
                } );
            } );
        }
    } );

    /****
     *
     * check password strength
     *
     *
     */


    var checkStrengthPointer;

    function checkStrength()
    {
        jsRoutes.controllers.WidgetAdmin.getPasswordMatch( authToken, $( "#newPassword" ).val() ).ajax( {
            success: function ( data )
            {
                if ( !data || data == "" ) {

                } else {
                    $( ".global-message" ).trigger( "showMessage", { class: 'error', msg: data} );
                }
            }
        } );
    }

    $( '#newPassword' ).keyup( function ()
    {
        if ( checkStrengthPointer ) {
            clearTimeout( checkStrengthPointer );
        }
        checkStrengthPointer = setTimeout( checkStrength, 1000 );
    } );


} );
