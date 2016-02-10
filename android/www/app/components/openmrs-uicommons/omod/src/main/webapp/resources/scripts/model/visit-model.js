(function($, _, OpenMRS) {
    OpenMRS.VisitModel = function(obj) {
        $.extend(this, obj);
    }

    OpenMRS.VisitModel.prototype = {
        constructor: OpenMRS.VisitModel,

        // in case there are multiple attributes, only returns the first one
        getAttribute: function(attributeTypeRefOrUuid) {
            var lookForUuid = attributeTypeRefOrUuid.uuid || attributeTypeRefOrUuid;
            return _.find(this.attributes, function(it) {
                return it.attributeType.uuid == lookForUuid;
            });
        },

        // in case there are multiple attributes, only returns the first one
        getAttributeValue: function(attributeTypeRefOrUuid) {
            var found = this.getAttribute(attributeTypeRefOrUuid);
            return found ? found.value : null;
        },

        active: function() {
            return (this.stopDatetime == null);
        },

        // only returns the first encounter of a certain type, or null if not found
        getEncounterByType: function(encounterTypeUuid) {
            return _.find(this.encounters, function(it) {
                return it.encounterType.uuid == encounterTypeUuid;
            })
        }
    }

})(jQuery, _, window.OpenMRS=window.OpenMRS||{});