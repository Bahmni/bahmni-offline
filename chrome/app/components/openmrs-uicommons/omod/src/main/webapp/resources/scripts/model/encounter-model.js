(function($, _, OpenMRS) {
    OpenMRS.EncounterModel = function(obj) {
        $.extend(this, obj);
    }

    OpenMRS.EncounterModel.prototype = {
        constructor: OpenMRS.EncounterModel,

        canBeDeletedBy: function(userModel) {
            return userModel.hasPrivilege("Task: emr.patient.encounter.delete");
        },

        canBeEditedBy: function(userModel) {
            return userModel.hasPrivilege("Task: emr.patient.encounter.edit");
        }
    }

})(jQuery, _, window.OpenMRS=window.OpenMRS||{});