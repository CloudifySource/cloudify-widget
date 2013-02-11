$(function(){
        var userId = $("body").attr("data-user-id");
        jsRoutes.controllers.DemosController.listWidgetForDemoUser( $("body").attr("data-user-id")).ajax({
            success:function( result ){
                        console.log([result]);
                        $("body").append($("#widget_nav").tmpl( {"widgets":result})).append($("#widgets_list").tmpl({"widgets":result, "userId":userId}));

                        var $firstButton = $("button[data-api-key]:first");
                        console.log([ $firstButton ]);
                        $firstButton.click();
                    },
            error:function( result ){ console.log([result])}
        });

    $("button[data-api-key]").live("click", function(){
        var $this = $(this);
        var apiKey = $this.attr("data-api-key");
        $(".recipe").hide();
        $(".recipe[data-api-key=" + apiKey + "]").show();
    });

});