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

  function instanceId() {
    return $.cookie("instanceId" + origin_page_url);
  }

  function write_log(message, class_name) {
    $("#log").append($("<li/>", {text: message}).addClass(class_name));
    $("#log").scrollTop($("#log")[0].scrollHeight);
  }

  function update_status() {
    $.get("/widget/"+ instanceId() + "/status?apiKey=" + apiKey, {}, function (data, textStatus, jqXHR) {
      if (data.status.state == "stopped") {
        $("#start_btn,#stop_btn").toggle();
        stop_instance();
      } else if (data.status.state == "error") {
        $("#start_btn,#stop_btn").toggle();
        write_log(data.status.message, "error");
        stop_instance();
      } else {
        if (window.log != data.status.output) {
          $("#log").empty();
          $.each(data.status.output, function(index, value) {
            write_log(value);
          });
          window.log = data.status.output;
        }
        $("#time_left_counter").text(data.status.timeleft + " minutes");
      }
    });
  }

  function stop_instance() {
    write_log("Server has stopped", "important");
    $("#time_left").hide();
    $("#links").hide();
    $.removeCookie("instanceId" + origin_page_url);
    $.removeCookie("publicIP" + origin_page_url);
    remove_status_update_timer();
  }

  function start_instance_btn_handler() {
    $("#start_btn,#stop_btn").toggle();
    if (!instanceId()) {
      $.post("/widget/start?apiKey=" + params["apiKey"], {}, function(data, textStatus, jqXHR) {
        if (data.status == "error") {
          $("#start_btn,#stop_btn").toggle();
          write_log(data.message, "error");
          return;
        }

        if (data.instance["@instanceId"]) {
          $.cookie("instanceId" + origin_page_url, data.instance["@instanceId"]);
          $.cookie("publicIP" + origin_page_url, data.instance["@publicIP"]);
          $("#time_left").show();
          set_status_update_timer();

          var link_info = data.instance.link;
          var custom_link = "<li id='custom_link'><a href='" + link_info.url + "' target='_blank'>" + link_info.title + "</a></li>";
          set_cloudify_dashboard_link(custom_link);
        }
      });
    }
  }

  function stop_instance_btn_handler() {
    if (!confirm("Are you sure you want to stop the instance?")) {
      return;
    }
    $("#start_btn,#stop_btn").toggle();
    if (instanceId()) {
      $.post("/widget/"+ instanceId() + "/stop?apiKey=" + params["apiKey"], {}, function (data) {
        if (data.status == "error") {
          $("#start_btn,#stop_btn").toggle();
          write_log(data.message, "error");
          return;
        }
        stop_instance();
      });
    }
  }

  function set_status_update_timer() {
    var status_update_frequency = 3000; // refresh every 3 sec
    window.status_update_timer = setInterval(update_status, status_update_frequency);
    update_status();
  }

  function remove_status_update_timer() {
    clearInterval(window.status_update_timer);
  }

  function set_cloudify_dashboard_link(custom_link) {
    $("#links").show();
    $("#cloudify_dashboard_link").attr("href", "http://" + $.cookie("publicIP" + origin_page_url) + ":8099/");
    $.cookie("custom_link", custom_link);
    if ($("#custom_link").get(0))
      $("#custom_link").replaceWith(custom_link);
    else
      $("#links").append($(custom_link));
  }

  var params = get_params();
  apiKey = params["apiKey"];
  origin_page_url = params["origin_page_url"];
  $("#facebook_share_link").attr("href", "http://www.facebook.com/sharer/sharer.php?u=" + encodeURI(origin_page_url));
  $("#google_plus_share_link").attr("href", "https://plus.google.com/share?url=" + encodeURI(origin_page_url));
  $("#twitter_share_link").attr("href", "https://twitter.com/share?url=" + encodeURI(origin_page_url));

  $("#title").text(decodeURIComponent(params["title"]));

  if (params["video_url"]) {
    $("#video_container").append($("<iframe id='youtube_iframe' width='270' height='160' frameborder='0' allowfullscreen></iframe>"))
    $("#youtube_iframe").attr("src", decodeURIComponent(params["video_url"]));
  }

  if (instanceId()) {
    $("#start_btn,#stop_btn,#time_left").toggle();
    set_cloudify_dashboard_link($.cookie("custom_link"));
    set_status_update_timer();
  }

  $("#start_btn").click(start_instance_btn_handler);
  $("#stop_btn").click(stop_instance_btn_handler);

  $("#show_advanced, #hide_advanced").click(function () {
    $("#advanced").toggle();
  });

  $(".share_link").click(function (e) {
    e.preventDefault();
    var leftvar = (screen.width-600)/2;
    var topvar = (screen.height-600)/2;
    window.open($(e.target).parents("a").attr("href"), '', 'menubar=no,toolbar=no,resizable=yes,scrollbars=yes,height=600,width=600,left='+ leftvar +',top=' + topvar);
  });
});
