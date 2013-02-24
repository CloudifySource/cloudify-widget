/*
 * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
 * <p/>
 * The software source code is proprietary and confidential information of GigaSpaces.
 * You may use the software source code solely under the terms and limitations of
 * the license agreement granted to you by GigaSpaces.
 */

$( function ()
{
    $( document ).ajaxError(function ( event, jqXHR, ajaxSettings, thrownError )
    {
        console.log( ["handling error", jqXHR.getAllResponseHeaders(), jqXHR.getResponseHeader( "session-expired" )] );
        if ( jqXHR.getResponseHeader( "session-expired" ) ) { // this should handle all session-expired problems
            console.log( "redirecting to login since session expired" );


            window.location.href = "/admin/signin?message=Session Expired";
            return;
        }
        if ( jqXHR.getResponseHeader( "display-message" ) ) {
            try {
                console.log(["handling field-error-message", arguments]);
                var displayMessage = JSON.parse( jqXHR.getResponseHeader( "display-message" ) );
                $( ".global-message" ).trigger( "showMessage", displayMessage );
                if ( ajaxSettings.form && displayMessage.formErrors ){
                    var $form = $(ajaxSettings.form)
                    var formField = null;
                    for ( formField in displayMessage.formErrors ){
                        var msg = displayMessage.formErrors[formField];
                        $form.find("[name=" + formField + "]").closest(".control-group").addClass("error").popover({content:msg});
                    }
                }
                return;
            } catch ( e ) {
                console.log( ["error showing message", e] );
            }
        }
        // console.log(["ajax error occurred", arguments]);
        $( ".global-message" ).trigger( "showMessage", {"msg": "Error - please try again later", "class": "error" } );
    } ).ajaxSuccess(function ( event, jqXHR, ajaxSettings )
        {

            if ( jqXHR.getResponseHeader( "display-message" ) ) {
                var displayMessage = jqXHR.getResponseHeader( "display-message" );
                console.log( ["handling display message", displayMessage] );
                $( ".global-message" ).trigger( "showMessage", JSON.parse( displayMessage ) );
            }
        } ).ajaxStart( function ()
        {
            $( ".global-message" ).trigger( "clearMessage" );
        } );



    // auto-render elements on page
    $(".info-icon" ).tooltip();
} );