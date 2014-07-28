$(function(){
    var email = $("#email").val();
    var userId = $("#userId").val();
    var opener = window.opener;
    console.log(["user is logged in, sending message", email, userId]);
    var $iframe=$(opener.document).find("iframe");
    var userData = { "name": "user_login", "email": email , userId : userId};
    console.log(["user is logged in ", userData ]);
//    $iframe.attr("src", $iframe.attr("src") + "&userId=" + encodeURIComponent(userId));
    $.postMessage( JSON.stringify({"name":"user_login", "userId" : userId }),document.location.origin, $iframe[0].contentWindow);
    $.postMessage( JSON.stringify({"name" : "play_widget"}),document.location.origin, $iframe[0].contentWindow);
    window.close();
});