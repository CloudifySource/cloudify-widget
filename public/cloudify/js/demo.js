$(function(){
        jsRoutes.controllers.DemosController.listWidgetForDemoUser( $("body").attr("data-user-id")).ajax({
            success:function( result ){
                        console.log([result]);
                        $("body").append($("widget_nav").tmpl(result)).append($("#widgets_list").tmpl(result));
                    },
            error:function( result ){ console.log([result])}
        });
});