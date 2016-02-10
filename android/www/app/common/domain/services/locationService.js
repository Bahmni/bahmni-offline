'use strict';

angular.module('bahmni.common.domain')
    .factory('locationService', ['$http', '$bahmniCookieStore', function ($http, $bahmniCookieStore) {
        var getAllByTag = function (tags) {
            return $http.get(Bahmni.Common.Constants.locationUrl, {
                params: {s: "byTags", q: tags || "", v:"default"},
                cache: true
            });
        };

        var getByUuid = function(locationUuid) {
            return $http.get(Bahmni.Common.Constants.locationUrl + "/" + locationUuid, {
                cache: true
            }).then(function (response) {
                return response.data;
            });
        };

        var getLoggedInLocation = function() {
            var cookie = $bahmniCookieStore.get(Bahmni.Common.Constants.locationCookieName);
            return getByUuid(cookie.uuid);
        };

        return {
            getAllByTag: getAllByTag,
            getLoggedInLocation: getLoggedInLocation,
            getByUuid: getByUuid
        };

    }]);
