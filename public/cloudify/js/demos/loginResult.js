$(function(){
    var email = $("#email").val();
    var userId = $("#userId").val();
    var opener = window.opener;
    console.log(["user is logged in, sending message", email, userId]);
    var $iframe=$(opener.document).find("iframe");
    var userData = { "name": "userlogin", "email": email , userId : userId};
    console.log(["user is logged in ", userData ]);
    $iframe.attr("src", $iframe.attr("src") + "&userId=" + encodeURIComponent(userId));
});