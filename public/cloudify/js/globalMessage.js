/*
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * <p/>
 * The software source code is proprietary and confidential information of GigaSpaces.
 * You may use the software source code solely under the terms and limitations of
 * the license agreement granted to you by GigaSpaces.
 */

$(function(){

    $(".global-message" ).bind("showMessage", function( e, conf){
//        debugger;
            $( this ).empty()
                          .append( $( "<div/>" )
                              .addClass( "alert" )
                              .addClass( "alert-" + (conf.class || "success").toLowerCase() )
                              .append( $( "<button/>" )
                                  .attr( "type", 'button' )
                                  .addClass( 'close' )
                                  .attr( "data-dismiss", 'alert' )
                                  .html( '&times;' ) )
                              .append( $( "<span/>" ).text( conf.msg ) ) )
                          .find(".alert" ).alert()})
                .bind("clearMessage", function( e, conf){
                        $( this ).empty();
                });


})