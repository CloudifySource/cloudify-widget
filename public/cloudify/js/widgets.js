jQuery.fn.reset = function () {
  $(this).each (function() { this.reset(); });
}

$(function () {
  function remove_session() {
    $.removeCookie("authToken");
    $.removeCookie("username");
    $.removeCookie("admin");
    window.location.href = "/admin/signin"; // todo : ?? -- what is this method? --
  }

  function render_summary() {
    $("#summary").show();
    $("#summary ul").empty();
    $.get("/widget/summary?authToken=" + authToken, {}, function (data) {
      if (data.status == "session-expired") {
        remove_session();
        return
      }

      $.each(data.summary.attributes, function(index, attr) {
        $("#summary ul").append("<li><span class='count'>" + attr.value + "</span>" + attr.name + "</li>");
      });
    });
  }

  function render_widget(widget) {
    var widget_tr = $("#widget_record").tmpl({
      admin: admin,
      id: widget["@id"],
      api_key: widget["@apiKey"],
      product_name: widget["@productName"],
      product_version: widget["@productVersion"],
      url: widget["@providerURL"],
      title: widget["@title"],
      user_name: widget["@userName"],
      recipe_url: widget["@recipeURL"],
      video_url: widget["@youtubeVideoUrl"],
      console_url: widget["@consoleurl"],
      launches: widget["@launches"],
      enabled: widget["@enabled"],
      domain: document.domain,
      instance_count: (widget.instances ? widget.instances.length : 0)
    });

    if (widget["@enabled"] == "true") {
      widget_tr.find(".disable_widget_btn").show();
      widget_tr.find(".instances_btn").show();
    } else {
      widget_tr.find(".enable_widget_btn").show();
      widget_tr.find(".disabled_marker").show();
    }

    if (widget.instances) {
      instance_search_cache[widget["@id"]] = widget_tr.find(".instance_search").quicksearch("#widget_" + widget["@id"] + "_instances tr");
      var instances_container = widget_tr.find("#widget_" + widget["@id"] + "_instances");

      $.each(widget.instances, function(index, instance) {
        var instance = $("#widget_instance_tmpl").tmpl({
          instance_id: instance["@instanceId"],
          anonymouse: instance["@anonymouse"],
          public_ip: instance["@publicIP"]
        });
        instances_container.append(instance);
      });
    }

    return widget_tr;
  }

  function parse_url(url) {
    var params = [], hash;
    var hashes = url.slice(url.indexOf('?') + 1).split('&');
    for(var i = 0; i < hashes.length; i++)
    {
      hash = hashes[i].split('=');
      params.push(hash[0]);
      params[hash[0]] = hash[1];
    }
    return params;
  }

  function youtube_parser(url) {
    if (url.indexOf("/embed/") == -1) {
      var video_id = parse_url(url)["v"];
      if (!video_id)
        return null;
      return "http://www.youtube.com/embed/" + video_id;
    } else
      return url;
  }

  window.update_widget_list = function() {
    $("#widget_list").empty();
    $.get("/widget/list?authToken=" + authToken, {}, function (data) {
      if (data.status == "session-expired") {
        remove_session();
        return
      }

      if (data.list.length == 0) {
        $("#welcome_window").show();
        $("#search_panel").hide();
        $("#widgets_panel").hide();
        $("#summary").hide();
        return;
      }

      $("#welcome_window").hide();
      $("#search_panel").show();
      $("#widgets_panel").show();

      $.each(data.list, function(index, widget) {
        render_widget(widget).appendTo("#widget_list");
        quicksearch.cache();
        $.each(instance_search_cache, function (i, instance_cace) { instance_cace.cache(); });

        $("#widget_" + widget["@id"] + "_instances_modal").on("show", function () {
          clearInterval(update_interval);
        }).on("hide", function () {
          window.update_interval = setInterval(update_widget_list, update_time_interval);
        });

        $("#widget_" + widget["@id"] + "_get_embed_modal").on("show", function () {
          clearInterval(update_interval);
        }).on("hide", function () {
          window.update_interval = setInterval(update_widget_list, update_time_interval);
        });
      });

      render_summary();
    });
  };

  window.authToken = $.cookie("authToken");
  window.username = $.cookie("username");
  window.admin = $.cookie("admin");
  window.instance_search_cache = {};
  window.update_time_interval = 60000;

  // edit it to edit the new widget form tips
  var field_tips = {
    productName: "The name of your product",
    productVersion: "The version of your product",
    title: "The widget title as it will appear when displaying the widget within a web page",
    youtubeVideoUrl: "URL of a YouTube video you want to display within the widget (Optional)",
    providerURL: "The URL of the product owner, e.g. http://www.mongodb.org",
    recipeURL: "A URL (http/https)to the recipe zip file",
    consolename: "The title of the link to the product dashboard / UI in the widget console",
    consoleurl: "The URL to the product dashboard / UI. Use $HOST as the hostname placeholder, e.g.: http://$HOST:8080/tomcat/index.html"
  };

  if (!authToken) {
    window.location.href = "/admin/signin"; // todo : ??? -- login verification?? -- should be on server side.
  }

  $("#username").text(username);
  if (admin) {
      $("#user_name_column").show();
  }

  for (var key in field_tips) {
    if (field_tips[key]) {
      $("#" + key).after($("<i class='info-icon' id='" + key +"_info'></i>"));
      $("#" + key + "_info").tooltip({title: field_tips[key]});
    }
  }

  window.quicksearch = $('input#search').quicksearch("#widget_list tr");
  window.update_interval = setInterval(update_widget_list, update_time_interval);
  update_widget_list();

  $(".required_mark").attr("title", "This field is mandatory").tooltip();

  $("#new_widget_form").submit(function (e) {
    e.preventDefault();

    var error = false;
    $("#new_widget_form").find("input.required" ).each( function (index, object) {
      if ($(object).val() == "") {
        error = true;
        $(object).parents(".control-group").addClass("error");
      }
    });

      var $youtubeVideoUrl = $("#youtubeVideoUrl");
      var youtube_video_url = $youtubeVideoUrl.val();
    if (youtube_video_url) {
      youtube_video_url = youtube_parser(youtube_video_url);
      if (youtube_video_url) {
        $youtubeVideoUrl.val(youtube_video_url);
        $youtubeVideoUrl.parents(".control-group").removeClass("error");
      } else {
        error = true;
        $youtubeVideoUrl.parents(".control-group").addClass("error");
        alert("Youtube video url is incorrect");
      }
    }

    if (!error) {
      $.post("/widget/new?authToken=" + authToken + "&" + $(e.target).serialize(), {}, function (data) {
        if (data.status == "session-expired") {
          remove_session();
          return
        }
        if (data.widget) {
          $(e.target).reset();
          $("#welcome_window").hide();
          $("#search_panel").show();
          $("#widgets_panel").show();
          render_summary();
          render_widget(data.widget).appendTo("#widget_list");
          $('#new_widget_modal').modal('hide');
          quicksearch.cache();
        } else {
          alert("Failed to save the widget: " + data.error);
        }
      });
    }
  });

    function setEnabledButtons( widget_container, enabled ){
        widget_container.find(".disable_widget_btn, .instances_btn").hide();
        widget_container.find(".enable_widget_btn, .disabled_marker").show();
    }

  $(".disable_widget_btn").live("click", function (e) {
    var widget_container = $(e.target).parents("tr.widget");
    var api_key = widget_container.data("api_key");
    if (confirm("Are you sure you want to disable widget " + api_key + "?")) {
      $.post("/widget/disable?authToken=" + authToken + "&apiKey=" + api_key, {}, function (data) {
        if (data.status == "session-expired") {
          remove_session();
          return
        }

        setEnabledButtons( widget_container, false);
      });
    }
  });

    function toUrlParams( data ){
          return $.param(data);
      }

  $(".enable_widget_btn").live("click", function (e) {
    var widget_container = $(e.target).parents("tr.widget");
    var postData = { apiKey : widget_container.data("api_key"), authToken: authToken };
    $.post("/widget/enable?" + toUrlParams( postData ), {}, function (data) {
      if (data.status == "session-expired") {
        remove_session();
        return
      }
      widget_container.removeClass("error");
      setEnabledButtons( widget_container, true);

    });
  });

  $(".regenerate_key_btn").live("click", function (e) {
    var widget_container = $(e.target).parents("tr.widget");
    var api_key = widget_container.data("api_key");
    if (confirm("Are you sure you want to regenerate api key for " + api_key + "?")) {
      $.post("/widget/regenerate?authToken=" + authToken + "&apiKey=" + api_key, {}, function (data) {
        if (data.status == "session-expired") {
          remove_session();
          return
        }
        $("#widget_" + data.widget["@id"]).replaceWith(render_widget(data.widget));
        quicksearch.cache();
      });
    }
  });

  $(".shutdown_instance").live("click", function (e) {
    var instance_container = $(e.target).parents("tr.instance");
    var instance_id = instance_container.data("instance_id");
    if (confirm("Are you sure you want to shutdown the instance " + instance_id + "?")) {
      $.post("/widget/" + instance_id + "/shutdown?authToken=" + authToken, {}, function (data) {
        if (data.status == "session-expired") {
          remove_session();
          return
        }
        instance_container.fadeOut('fast', function(){
          instance_container.remove();
        });
      });
    }
  });
});
