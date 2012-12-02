$(function () {
  function show_message(text, alert_class) {
    $("#message").empty();
    $("#message").append($("<div class='alert alert-" + (alert_class || "success") + "'><button type='button' class='close' data-dismiss='alert'>×</button>" + text + "</div>"));
    $(".alert").alert();
  }

  $("#signin_form").submit(function (e) {
    e.preventDefault();
    $(".alert").alert("close");
    var username = $("input[name=username]").val();
    var password = $("input[name=password]").val();
    var url = "/signin?email=" + username + "&password=" + password;
    $.post(url, {}, function (data) {
      if (data.status == "error") {
        show_message(data.message, "error");
        $("input[name=password]").val("");
        $("input[name=password_confirmation]").val("");
        $("input[name=password]").focus();
      } else {
        $.cookie("admin", data.session["@admin"], { expires: 1 });
        $.cookie("authToken", data.session["@authToken"], { expires: 1 });
        $.cookie("username", username, { expires: 1 });
        window.location.href = "widgets.html"; // /admin/widgets.html
      }
    });
  });

  $("#signup_form").submit(function (e) {
    e.preventDefault();
    $(".alert").alert("close");

    var error = false;
    $.each($("#signup_form input.required"), function (index, object) {
      if ($(object).val() == "") {
        error = true;
        $(object).addClass("error");
      }
    });

    if (error) return;

    var password = $("input[name=password]").val();
    var password_confirmation = $("input[name=password_confirmation]").val();

    if (password != password_confirmation) {
      show_message("Passwords do not match", "error");
      $("input[name=password_confirmation]").val("");
      $("input[name=password_confirmation]").focus();
      return
    }

    var username = $("input[name=username]").val();
    var password = $("input[name=password]").val();
    var firstname = $("input[name=firstname]").val();
    var lastname = $("input[name=lastname]").val();
    var url = "/signup?email=" + username + "&password=" + password + "&firstname=" + firstname + "&lastname=" + lastname;
    $.post(url, {}, function (data) {
      if (data.status == "error") {
        show_message(data.message, "error");
        $("input[name=password]").val("");
        $("input[name=password_confirmation]").val("");
        $("input[name=password]").focus();
      } else {
        $.cookie("authToken", data.session["@authToken"], { expires: 1 });
        $.cookie("username", username, { expires: 1 });
        window.location.href = "widgets.html"
      }
    });
  });

  $("input[name=username]").focus();
});
