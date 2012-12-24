$(function () {

    var $alert = $(".alert");

  function show_message(text, alert_class) {
      $( "#message" ).empty()
          .append( $( "<div/>" )
              .addClass( "alert" )
              .addClass( "alert-" + (alert_class || "success") )
              .append( $( "<button/>" )
                  .attr( "type", 'button' )
                  .addClass( 'close' )
                  .attr( "data-dismiss", 'alert' )
                  .html( '&times;' ) )
              .append( $( "<span/>" ).text( text ) ) );
    $alert.alert();
  }

  function toUrlParams( data ){
      return $.param(data);
  }

    function before(e){
        e.stopPropagation();
        $alert.alert("close");
    }

  function fillFormData( keys , $form ){
      return $form.formParams();
//      var data = {};
//      $.each(keys, function(index,item){
//          data[item] = $form.find("[name=" + v + "]").val();
//      });
//      return data;
  }

  $("#signin_form").submit(function (e) {
      before(e);
        var formData = fillFormData(["email","password"], $(this));

    var url = "/signin?" + toUrlParams(formData);
    $.ajax({ type:'POST',
        url:url,
        global:false,
        data:{},
        success: function (data) {
                    if (data.status == "error") {
                    show_message(data.message, "error");
                    $("input[name=password]").val("" ).focus();
                } else {
                    $.cookie("admin", data.session["@admin"], { expires: 1, path:'/' });
                    $.cookie("authToken", data.session["@authToken"], { expires: 1 , path:'/'});
                    $.cookie("username", formData.email, { expires: 1 , path:'/'});
                    window.location.href = "widgets.html"; // /admin/widgets.html
                }
         },
        error: function(data){
            show_message("Invalid Credentials","error");
        }
    });
      return false;
  });

  $("#signup_form").submit(function (e) {
    before(e);

    var error = false;
    $("#signup_form" ).find("input.required" ).each( function (index, object) {
      if ( $.trim($(object).val()) == "") {
        error = true;
        $(object).addClass("error");
      }
    });

    if (error) return false;

    var $password = $("input[name=password]");
    var $passwordConfirmation=$("input[name=password_confirmation]");

    var formData= fillFormData( ["password", "passwordConfirmation", "username", "firstname", "lastname"] , $(this) );

    if ( formData.password != formData.passwordConfirmation ) { // TODO : guy - get rid of this here. do it on server side.
      show_message("Passwords do not match", "error");
        $passwordConfirmation.val("" ).focus();
      return false;
    }

    var url = "/signup?" + toUrlParams( formData );
    $.post(url, {}, function (data) {
      if (data.status == "error") {
        show_message(data.message, "error");
        $passwordConfirmation.val("");
        $password.val("" ).focus();

      } else {
        $.cookie("authToken", data.session["@authToken"], { expires: 1 , path:'/'});
        $.cookie("username", username, { expires: 1, path:'/' });
        window.location.href = "widgets.html"
      }
    });
      return false;
  });


  $("#reset_password_form" ).submit( function(e){
      before(e);
      $.ajax({
          type:'POST',
          url:"/admin/resetPassword?" + toUrlParams( fillFormData(["email","h"], $(this) ) ),
          data: fillFormData( ["h", "email"], $(this)),
          success:function(){ show_message("Check your inbox.")},
          error:function(result){ console.log(result.responseText);}
      });
      return false;
  });

  $("input[name=username]").focus();
});
