/**
    Project: Collapsible Checkbox Tree jQuery Plugin
    Version: 1.0.1
        Author: Lewis Jenkins
        Website: http://www.redcarrot.co.uk/2009/11/11/collapsible-checkbox-tree-jquery-plugin/

    License:
        The CheckTree jQuery plugin is currently available for use in all personal or
        commercial projects under both MIT and GPL licenses. This means that you can choose
        the license that best suits your project and use it accordingly.
*/
(function($) {

        $.fn.collapsibleCheckboxTree = function(options) {

                var defaults = {
                        initialState : 'collapse' // Options - 'expand' (fully expanded), 'collapse' (fully collapsed) or default
                };

                var options = $.extend(defaults, options);

                this.each(function() {

                        var $root = this;

                        // Add button
                        $(this).before('<button id="expand">  + >>>  </button> &nbsp; <button id="collapse">  - <<<  </button>');

                        // Hide all except top level
//                      $("ul", $(this)).addClass('hide');
                        $("li", $(this)).addClass('hide');


                        // Add tree links
                        $("li", $(this)).prepend('<span class="something">&nbsp;</span>');
                        $("li:has(> ul:not(.hide)) > span", $(this)).addClass('expanded').html('<div style="float:left; width:30px;">[ - ]</div>');
                        $("li:has(> ul.hide) > span", $(this)).addClass('collapsed').html('<div style="float:left; width:30px;">[ + ]</div>');

                        // Tree function
                        $("li:has(> ul) span", $(this)).click(function(){


                                // If was previously collapsed...
                                if ($(this).is(".collapsed")) {

                                        // ... then expand
                                        $("> ul", $(this).parent("li")).removeClass('hide');

                                        // ... and update the html
                                        $(this).removeClass("collapsed").addClass("expanded").html('<div style="float:left; width:30px;">[ - ]</div>');

                                // If was previously expanded...
                                } else if ($(this).is(".expanded")) {

                                        // ... then collapse
                                        $("> ul", $(this).parent("li")).addClass('hide');
                                        // and update the html
                                        $(this).removeClass("expanded").addClass("collapsed").html('<div style="float:left; width:30px;">[ + ]</div>');
                                }

                        });

                        // Button functions

                        // Expand all
                        $("#expand").click(function () {
                                // Show all children
                                $("ul", $root).removeClass('hide');
                                // and update the html
                                $("li:has(> ul) > span", $root).removeClass("collapsed").addClass("expanded").html('<div style="float:left; width:30px;">[ - ]</div>');
                                return false;
                        });
                        // Collapse all
                        $("#collapse").click(function () {
                                // Hide all children
//                                $("ul", $root).addClass('hide');
                                  $("ul", "li").addClass('hide');

                                // and update the html
                                $("li:has(> ul) > span", $root).removeClass("expanded").addClass("collapsed").html('<div style="float:left; width:30px;">[ + ]</div>');
                                return false;
                        });
                        // Wrap around checked boxes
                        $("#default").click(function () {
                                // Hide all except top level
//                                $("ul", $root).addClass('hide');
                                $("li", $root).addClass('hide');

                                // and update the html
                                $("li:has(> ul:not(.hide)) > span", $root).removeClass('collapsed').addClass('expanded').html('<div style="float:left; width:30px;">[ - ]</div>');
                                $("li:has(> ul.hide) > span", $root).removeClass('expanded').addClass('collapsed').html('<div style="float:left; width:30px;">[ + ]</div>');
                                return false;
                        });

                        switch(defaults.initialState) {
                                case 'expand':
                                        $("#expand").trigger('click');
                                        break;
                                case 'collapse':
                                        $("#collapse").trigger('click');
                                        break;
                        }

                });

                return this;

        };

})(jQuery);

jQuery(document).ready(function()
{
    $('div#aspect_artifactbrowser_CommunityBrowser_div_comunity-browser').collapsibleCheckboxTree();
});

