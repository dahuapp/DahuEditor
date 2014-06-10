/**
 * Created by dufourau on 6/5/14.
 */
/**
 * Created by dufourau on 6/5/14.
 */
define([
    'underscore',
    'backbone',
    'models/action'
], function(_, Backbone, ActionModel){

    /**
     *  Model of image object
     */
    var MoveModel = ActionModel.extend({
        defaults: {
            type: 'move',
            target:null,
            trigger: null,
            finalAbs: null,
            finalOrd: null
        }

    });

    return MoveModel;
});