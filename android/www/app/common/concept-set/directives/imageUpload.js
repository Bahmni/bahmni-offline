angular.module('bahmni.common.conceptSet')
    .directive('imageUpload', ['$rootScope', 'visitDocumentService', 'spinner', function ($rootScope, visitDocumentService, spinner) {

        var link = function (scope, element) {
            element.bind("change", function () {
                var file = element[0].files[0];
                var reader = new FileReader();
                reader.onload = function (event) {
                    var image = event.target.result;
                    spinner.forPromise(visitDocumentService.saveFile(image, scope.patientUuid).then(function(response) {
                        scope.url = response.data;
                        element.val(null);
                    }));
                };
                reader.readAsDataURL(file);
            });
        };

        return {
            restrict: 'A',
            require: 'ngModel',
            scope: {
                url:"=ngModel",
                patientUuid: "="
            },
            link: link
        }
    }]);