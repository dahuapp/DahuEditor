/**
 * Created by nabilbenabbou1 on 28/05/2014.
 */

define([
    'handlebars',
    'backbone.marionette',
    'text!templates/views/workspace/background.html',
    'modules/requestResponse'
], function(Handlebars, Marionette, Filmstrip_background_tpl, reqResponse){

    /**
     * Screen background view
     */
    var backgroundView = Marionette.ItemView.extend({
        template: Handlebars.default.compile(Filmstrip_background_tpl),
        templateHelpers: {
            // The name of the picture's full path
            imgFullPath: function() {
                return reqResponse.request("app:screencast:controller").getImgFullPath(this.img);
            }
        }
    });

    return backgroundView;
});