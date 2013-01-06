$(function () {

    function show_message(text, alert_class) {
        $("#message" ).trigger("showMessage", { "msg" : text, "class" : alert_class})
      }


  function toUrlParams( data ){
      return $.param(data);
  }

    function before(e){
        e.stopPropagation();
        $(".alert").alert("close");
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
    console.log("I am about to login");
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
                    console.log(["after login",data]);
                    $.cookie("admin", data.session["@admin"], { expires: 1, path:'/' });
                    $.cookie("authToken", data.session["@authToken"], { expires: 1 , path:'/'});
                    $.cookie("username", formData.email, { expires: 1 , path:'/'});
                    window.location.href = "/admin/widgets"; // /admin/widgets.html
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

    var formData= fillFormData( ["password", "passwordConfirmation", "email", "firstname", "lastname"] , $(this) );

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
        $.cookie("username", formData.email , { expires: 1, path:'/' });
        window.location.href = "/admin/widgets";
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
          success:function(){ window.location.href = "/admin/signin?message=Check your inbox."},
          error:function(result){ console.log(result.responseText);}
      });
      return false;
  });

  $("input[name=username]").focus();



    // check signup password field value strength.

    var checkStrengthPointer;
        function checkStrength(){
            var $signup_form = $("#signup_form");
            var formData = fillFormData(["password","email"], $signup_form );
            jsRoutes.controllers.WidgetAdmin.checkPasswordStrength ( formData.password, formData.email ).ajax({});
        }

        $( '#signup_form').find('[name=password]' ).keyup( function ()
        {
            if ( checkStrengthPointer ){
                clearTimeout( checkStrengthPointer );
            }
            checkStrengthPointer = setTimeout( checkStrength , 1000 );
        });



});
