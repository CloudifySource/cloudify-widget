/*
 * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
 * <p/>
 * The software source code is proprietary and confidential information of GigaSpaces.
 * You may use the software source code solely under the terms and limitations of
 * the license agreement granted to you by GigaSpaces.
 */

/**
 * This is our JS file to override things from Angular.
 *
 */


// enhance the popover model to support mouseenter/mouseleave instead of click.
angular.module( 'ui.bootstrap.popover', [] ).directive( 'popoverPopup', function () {
  return {
    restrict: 'EA',
    replace: true,
    scope: { popoverTitle: '@', popoverContent: '@', placement: '@', animation: '&', isOpen: '&' },
    templateUrl: 'template/popover/popover.html'
  };
} ).directive( 'popover', [ '$compile', '$timeout', '$parse', '$window', function ( $compile, $timeout, $parse, $window ) {

  var template =
    '<popover-popup '+
      'popover-title="{{tt_title}}" '+
      'popover-content="{{tt_popover}}" '+
      'placement="{{tt_placement}}" '+
      'animation="tt_animation()" '+
      'is-open="tt_isOpen"'+
      '>'+
    '</popover-popup>';

  return {
    scope: true,
    link: function ( scope, element, attr ) {
      var popover = $compile( template )( scope ),
          transitionTimeout;

      attr.$observe( 'popover', function ( val ) {
        scope.tt_popover = val;
      });

      attr.$observe( 'popoverTitle', function ( val ) {
        scope.tt_title = val;
      });

      attr.$observe( 'popoverPlacement', function ( val ) {
        // If no placement was provided, default to 'top'.
        scope.tt_placement = val || 'top';
      });

      attr.$observe( 'popoverAnimation', function ( val ) {
        scope.tt_animation = $parse( val );
      });

      // By default, the popover is not open.
      scope.tt_isOpen = false;

      // Calculate the current position and size of the directive element.
      function getPosition() {
        var boundingClientRect = element[0].getBoundingClientRect();
        return {
          width: element.prop( 'offsetWidth' ),
          height: element.prop( 'offsetHeight' ),
          top: boundingClientRect.top + $window.pageYOffset,
          left: boundingClientRect.left + $window.pageXOffset
        };
      }

      function show() {
        var position,
            ttWidth,
            ttHeight,
            ttPosition;

        // If there is a pending remove transition, we must cancel it, lest the
        // toolip be mysteriously removed.
        if ( transitionTimeout ) {
          $timeout.cancel( transitionTimeout );
        }

        // Set the initial positioning.
        popover.css({ top: 0, left: 0, display: 'block' });

        // Now we add it to the DOM because need some info about it. But it's not
        // visible yet anyway.
        element.after( popover );

        // Get the position of the directive element.
        position = getPosition();

        // Get the height and width of the popover so we can center it.
        ttWidth = popover.prop( 'offsetWidth' );
        ttHeight = popover.prop( 'offsetHeight' );

        // Calculate the popover's top and left coordinates to center it with
        // this directive.
        switch ( scope.tt_placement ) {
          case 'right':
            ttPosition = {
              top: (position.top + position.height / 2 - ttHeight / 2) + 'px',
              left: (position.left + position.width) + 'px'
            };
            break;
          case 'bottom':
            ttPosition = {
              top: (position.top + position.height) + 'px',
              left: (position.left + position.width / 2 - ttWidth / 2) + 'px'
            };
            break;
          case 'left':
            ttPosition = {
              top: (position.top + position.height / 2 - ttHeight / 2) + 'px',
              left: (position.left - ttWidth) + 'px'
            };
            break;
          default:
            ttPosition = {
              top: (position.top - ttHeight) + 'px',
              left: (position.left + position.width / 2 - ttWidth / 2) + 'px'
            };
            break;
        }

        // Now set the calculated positioning.
        popover.css( ttPosition );

        // And show the popover.
        scope.tt_isOpen = true;
      }

      // Hide the popover popup element.
      function hide() {
        // First things first: we don't show it anymore.
        //popover.removeClass( 'in' );
        scope.tt_isOpen = false;

        // And now we remove it from the DOM. However, if we have animation, we
        // need to wait for it to expire beforehand.
        // FIXME: this is a placeholder for a port of the transitions library.
        if ( angular.isDefined( scope.tt_animation ) && scope.tt_animation() ) {
          transitionTimeout = $timeout( function () { popover.remove(); }, 500 );
        } else {
          popover.remove();
        }
      }

        // guy - todo get the event from directive.
      // Register the event listeners.
//      element.bind( 'click', function() {
//        if(scope.tt_isOpen){
//            scope.$apply( hide );
//        } else {
//            scope.$apply( show );
//        }
//
//      });

        element.bind('mouseenter', function(){ scope.$apply(show);});
        element.bind('mouseleave', function(){ scope.$apply(hide);});
    }
  };
}]);


// fixed case where no content - we do not display popup.
angular.module("template/popover/popover.html", []).run(["$templateCache", function($templateCache){

  $templateCache.put("template/popover/popover.html",
    "<div class=\"popover {{placement}}\" ng-class=\"{ in: isOpen(), fade: animation(), forceHide: !popoverContent.length }\">" +
    "  <div class=\"arrow\"></div>" +
    "" +
    "  <div class=\"popover-inner\">" +
    "      <h3 class=\"popover-title\" ng-bind=\"popoverTitle\" ng-show=\"popoverTitle\"></h3>" +
    "      <div class=\"popover-content\" ng-bind=\"popoverContent\"></div>" +
    "  </div>" +
    "</div>" +
    "");
}]);
