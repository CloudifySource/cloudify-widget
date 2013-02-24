$(function(){
    $("body").live("requireLogin", function(){
              console.log(["parent got play widget message, forcing user to login"]);
              var maxHeight = $(window.top).height();
                var maxWidth = $(window.top).width();
              var windowWidth = Math.min(1000, maxWidth);
              var windowHeight = Math.min(600,maxHeight) ;
              var windowLeft = ( maxWidth - windowWidth ) / 2;
              var windowTop = ( maxHeight - windowHeight ) / 2;
        var args = ['height=', windowHeight, 'width=', windowWidth, 'left=', windowLeft, 'px,top=' , windowTop , 'px'].join("");
        console.log(["new window details", args]);
        newwindow=window.open('/demos/loginWithGoogle','loginWithGoogle',args);
            	if (window.focus) {newwindow.focus()}
             	return false;
      });
});
