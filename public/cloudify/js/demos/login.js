$(function(){
            var originalCloudifyIframeSrc = null;
            var msgSource = $("#cloudify-widget").attr("src").split("/js")[0];
            console.log("registering event for " + msgSource );
            var callbacks = {};
            callbacks["playwidget"] = function(e){


                console.log(["parent got play widget message, forcing user to login",e]);

                	newwindow=window.open('/demos/loginWithGoogle','name','height=200,width=400');
                	if (window.focus) {newwindow.focus()}
                	return false;

//                var $iframe = $("iframe");
//                originalCloudifyIframeSrc = $iframe.attr("src");
//                $iframe.attr("src","/demos/loginWithGoogle");

            };

//            $("iframe").live("userlogin",function(e,data){
//
//                console.log(["after login, need to redirect back to the widget along with user ID. ",e]);
//                            debugger;
//                            alert("hello : " + data.email );
//                            $("iframe").attr("src", $("iframe").attr("src") + "&userId=" + encodeURIComponent(data.userId) );
//            });


            $.receiveMessage( function(e){

                    try{
                        var msg = JSON.parse(e.data);
                        callbacks[msg.name](e);
                    }catch(exc){ console.log(["problem invoking callback for ", e, exc, callbacks])};
                },
                function(origin){ return true; }
                ); // support for different domains
});