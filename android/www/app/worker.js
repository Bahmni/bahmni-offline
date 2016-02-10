(function (global) {
    'use strict';

    //replace array with pre-fetch-list surrounded by {} and run 'grunt pre-fetch' to regenerate array
    var preFetchList = ['/bahmni/lib/chart/c3-0.4.10.min.css','/bahmni/lib/chart/c3-0.4.10.min.js','/bahmni/lib/chart/d3-3.5.5.min.js','/bahmni/lib/jquery/images/animated-overlay.gif','/bahmni/lib/jquery/images/ui-bg_diagonals-thick_18_b81900_40x40.png','/bahmni/lib/jquery/images/ui-bg_diagonals-thick_20_666666_40x40.png','/bahmni/lib/jquery/images/ui-bg_flat_10_000000_40x100.png','/bahmni/lib/jquery/images/ui-bg_glass_100_f6f6f6_1x400.png','/bahmni/lib/jquery/images/ui-bg_glass_100_fdf5ce_1x400.png','/bahmni/lib/jquery/images/ui-bg_glass_65_ffffff_1x400.png','/bahmni/lib/jquery/images/ui-bg_gloss-wave_35_f6a828_500x100.png','/bahmni/lib/jquery/images/ui-bg_highlight-soft_100_eeeeee_1x100.png','/bahmni/lib/jquery/images/ui-bg_highlight-soft_75_ffe45c_1x100.png','/bahmni/lib/jquery/images/ui-icons_222222_256x240.png','/bahmni/lib/jquery/images/ui-icons_228ef1_256x240.png','/bahmni/lib/jquery/images/ui-icons_ef8c08_256x240.png','/bahmni/lib/jquery/images/ui-icons_ffd27a_256x240.png','/bahmni/lib/jquery/images/ui-icons_ffffff_256x240.png','/bahmni/lib/jquery/jquery-ui-1.10.4.custom.min.css','/bahmni/lib/jquery/jquery-ui-1.10.4.custom.min.js','/bahmni/lib/magnific-popup/jquery.magnific-popup.js','/bahmni/lib/magnific-popup/magnific-popup.css','/bahmni/lib/modernizr.custom.80690.js','/bahmni/images/bahmniLogo.png','/bahmni/images/bars.png','/bahmni/images/bed-available.png','/bahmni/images/bed-occupied.png','/bahmni/images/blank-user.gif','/bahmni/images/blank-user.png','/bahmni/images/blank.jpeg','/bahmni/images/close-bars.png','/bahmni/images/create.png','/bahmni/images/edit.png','/bahmni/images/form_sheetbg.png','/bahmni/images/glyphicons-halflings-white.png','/bahmni/images/glyphicons-halflings.png','/bahmni/images/icon-paper-clip.png','/bahmni/images/jss-icon.png','/bahmni/images/jss_logo.png','/bahmni/images/loader.gif','/bahmni/images/next.png','/bahmni/images/nhn-logo.png','/bahmni/images/patient-summary.png','/bahmni/images/patient-switch.png','/bahmni/images/photo.png','/bahmni/images/prev.png','/bahmni/images/refill-icon.png','/bahmni/images/spinner.gif','/bahmni/home/app.js','/bahmni/home/controllers/dashboardController.js','/bahmni/home/controllers/loginController.js','/bahmni/home/index.html','/bahmni/home/initialization.js','/bahmni/home/loginInitialization.js','/bahmni/home/offlineSyncInitialization.js','/bahmni/home/views/dashboard.html','/bahmni/home/views/login.html','/bahmni/registration/app.js','/bahmni/registration/constants.js','/bahmni/registration/controllers/createPatientController.js','/bahmni/registration/controllers/editPatientController.js','/bahmni/registration/controllers/navigationController.js','/bahmni/registration/controllers/patientCommonController.js','/bahmni/registration/controllers/searchController.js','/bahmni/registration/controllers/visitController.js','/bahmni/registration/defaults.js','/bahmni/registration/directives/addressFields.js','/bahmni/registration/directives/patientAction.js','/bahmni/registration/directives/patientRelationship.js','/bahmni/registration/directives/printOptions.js','/bahmni/registration/directives/topDownAddressFields.js','/bahmni/registration/index.html','/bahmni/registration/init.js','/bahmni/registration/initialization.js','/bahmni/registration/mappers/createPatientRequestMapper.js','/bahmni/registration/mappers/openmrsPatientMapper.js','/bahmni/registration/mappers/updatePatientRequestMapper.js','/bahmni/registration/models/age.js','/bahmni/registration/models/patient.js','/bahmni/registration/models/patientConfig.js','/bahmni/registration/models/preferences.js','/bahmni/registration/models/registrationEncounterConfig.js','/bahmni/registration/offlineRegistrationInitialization.js','/bahmni/registration/services/addressHierarchyService.js','/bahmni/registration/services/offlinePatientService.js','/bahmni/registration/services/patientAttributeService.js','/bahmni/registration/services/patientService.js','/bahmni/registration/services/registrationCardPrinter.js','/bahmni/registration/views/addressFields.html','/bahmni/registration/views/age.html','/bahmni/registration/views/customIdentifierConfirmation.html','/bahmni/registration/views/dob.html','/bahmni/registration/views/editpatient.html','/bahmni/registration/views/header.html','/bahmni/registration/views/layout.html','/bahmni/registration/views/newpatient.html','/bahmni/registration/views/nolayoutfound.html','/bahmni/registration/views/notimplemented.html','/bahmni/registration/views/patientAction.html','/bahmni/registration/views/patientDeathInformation.html','/bahmni/registration/views/patientRelationships.html','/bahmni/registration/views/patientcommon.html','/bahmni/registration/views/printOptions.html','/bahmni/registration/views/search.html','/bahmni/registration/views/topDownAddressFields.html','/bahmni/registration/views/visit.html','/bahmni/i18n/home/locale_en.json','/bahmni/i18n/home/locale_es.json','/bahmni/i18n/home/locale_fr.json','/bahmni/i18n/registration/locale_en.json','/bahmni/i18n/registration/locale_fr.json'];

    var configs = [
            '/bahmni_config/openmrs/apps/customDisplayControl/js/customControl.js',
            '/bahmni_config/openmrs/apps/home/app.json',
            '/bahmni_config/openmrs/apps/home/extension.json',
            '/bahmni_config/openmrs/i18n/home/locale_en.json',
            '/bahmni_config/openmrs/apps/registration/app.json',
            '/bahmni_config/openmrs/apps/registration/extension.json',
            '/bahmni_config/openmrs/i18n/registration/locale_en.json',
            '/bahmni_config/openmrs/apps/registration/fieldValidation.js'
        ],
        styles = [
            '/bahmni/styles/fonts/opensans-regular-webfont.ttf',
            '/bahmni/styles/fonts/fontawesome-webfont.woff2?v=4.3.0',
            '/bahmni/styles/fonts/opensans-bold-webfont.ttf',
            '/bahmni/styles/home.css',
            '/bahmni/styles/registration.css'
        ],
        modules = [
            '/',
            '/home',
            '/bahmni/home',
            '/bahmni/registration/'
        ],
        globalProperty = [
            '/openmrs/ws/rest/v1/bahmnicore/sql/globalproperty?property=locale.allowed.list',
            '/openmrs/ws/rest/v1/bahmnicore/sql/globalproperty?property=mrs.genders',
            '/openmrs/ws/rest/v1/bahmnicore/sql/globalproperty?property=bahmni.relationshipTypeMap',
            '/openmrs/ws/rest/v1/bahmnicore/sql/globalproperty?property=concept.reasonForDeath'
        ],
        rest = [
            '/openmrs/ws/rest/v1/location?q=Login+Location&s=byTags&v=default',
            '/openmrs/ws/rest/v1/bahmnicore/config/bahmniencounter?callerContext=REGISTRATION_CONCEPTS',
            '/openmrs/ws/rest/v1/personattributetype?v=custom:(uuid,name,sortWeight,description,format,concept)',
            '/openmrs/ws/rest/v1/idgen/identifiersources',
            '/openmrs/module/addresshierarchy/ajax/getOrderedAddressHierarchyLevels.form',
            '/openmrs/ws/rest/v1/relationshiptype?v=custom:(aIsToB,bIsToA,uuid)'
        ],
        others = //home page
        [
            '/bahmni/worker.js',
            '/bahmni/initWorker.js',
            '/bahmni/home/#/device/chrome-app',
            '/bahmni/components/select2/select2.css',
            '/bahmni/components/jquery/jquery.min.js',
            '/bahmni/components/lodash/dist/lodash.min.js',
            '/bahmni/components/jquery.cookie/jquery.cookie.js',
            '/bahmni/components/angular/angular.min.js',
            '/bahmni/components/angular-ui-router/release/angular-ui-router.min.js',
            '/bahmni/components/ngInfiniteScroll/build/ng-infinite-scroll.min.js',
            '/bahmni/components/stacktrace-js/dist/stacktrace.min.js',
            '/bahmni/components/ng-clip/dest/ng-clip.min.js',
            '/bahmni/components/zeroclipboard/dist/ZeroClipboard.min.js',
            '/bahmni/components/angular-translate/angular-translate.min.js',
            '/bahmni/components/angular-cookies/angular-cookies.min.js',
            '/bahmni/components/angular-translate-loader-static-files/angular-translate-loader-static-files.min.js',
            '/bahmni/components/angular-translate-storage-cookie/angular-translate-storage-cookie.min.js',
            '/bahmni/components/angular-translate-storage-local/angular-translate-storage-local.min.js',
            '/bahmni/components/angular-translate-handler-log/angular-translate-handler-log.min.js',
            '/bahmni/common/constants.js',
            '/bahmni/common/route-errorhandler/init.js',
            '/bahmni/common/util/init.js',
            '/bahmni/common/util/httpErrorInterceptor.js',
            '/bahmni/common/util/arrayUtil.js',
            '/bahmni/common/auth/init.js',
            '/bahmni/common/auth/user.js',
            '/bahmni/common/auth/userService.js',
            '/bahmni/common/auth/authentication.js',
            '/bahmni/common/config/init.js',
            '/bahmni/common/config/services/configurations.js',
            '/bahmni/common/domain/init.js',
            '/bahmni/common/domain/services/locationService.js',
            '/bahmni/common/domain/services/localeService.js',
            '/bahmni/common/domain/services/configurationService.js',
            '/bahmni/common/domain/mappers/observationValueMapper.js',
            '/bahmni/common/app-framework/init.js',
            '/bahmni/common/app-framework/models/appDescriptor.js',
            '/bahmni/common/app-framework/services/appService.js',
            '/bahmni/common/app-framework/services/mergeService.js',
            '/bahmni/common/ui-helper/init.js',
            '/bahmni/common/ui-helper/spinner.js',
            '/bahmni/common/ui-helper/directives.js',
            '/bahmni/common/ui-helper/controllers/messageController.js',
            '/bahmni/common/ui-helper/services/messagingService.js',
            '/bahmni/common/logging/init.js',
            '/bahmni/common/logging/exceptionHandler.js',
            '/bahmni/common/i18n/init.js',
            '/bahmni/common/i18n/bahmni-translate.js',
            '/bahmni/common/i18n/services/mergeLocaleFilesService.js',
            '/bahmni/common/ui-helper/controllers/dashboardController.js',
            '/bahmni/common/ui-helper/messages.html',
            '/bahmni/common/ui-helper/views/dashboard.html',
            // end of login page
            //Start of home dashboard
            //start of registration
            '/bahmni/components/ngDialog/css/ngDialog.min.css',
            '/bahmni/components/ngDialog/css/ngDialog-theme-default.min.css',
            '/bahmni/components/ngDialog/css/ngDialog-theme-plain.min.css',
            '/bahmni/components/angular-sanitize/angular-sanitize.min.js',
            '/bahmni/components/angular-recursion/angular-recursion.min.js',
            '/bahmni/components/moment/min/moment.min.js',
            '/bahmni/components/angular-bindonce/bindonce.min.js',
            '/bahmni/components/select2/select2.min.js',
            '/bahmni/components/angular-ui-select2/src/select2.js',
            '/bahmni/components/ngDialog/js/ngDialog.min.js',
            '/bahmni/components/angular-elastic/elastic.js',
            '/bahmni/common/util/dateUtil.js',
            '/bahmni/common/util/validationUtil.js',
            '/bahmni/common/util/dynamicResourceLoader.js',
            '/bahmni/common/util/arrayUtil.js',
            '/bahmni/common/util/httpErrorInterceptor.js',
            '/bahmni/common/models/visitControl.js',
            '/bahmni/common/config/services/configurations.js',
            '/bahmni/common/domain/observationFilter.js',
            '/bahmni/common/domain/mappers/conceptMapper.js',
            '/bahmni/common/domain/helpers/domainHelpers.js',
            '/bahmni/common/domain/services/visitService.js',
            '/bahmni/common/domain/services/encounterService.js',
            '/bahmni/common/domain/services/observationsService.js',
            '/bahmni/common/domain/services/providerService.js',
            '/bahmni/common/domain/encounterConfig.js',
            '/bahmni/common/domain/mappers/observationValueMapper.js',
            '/bahmni/common/ui-helper/directives/toggle.js',
            '/bahmni/common/ui-helper/directives/popOver.js',
            '/bahmni/common/ui-helper/directives/splitButton.js',
            '/bahmni/common/ui-helper/directives/focusOn.js',
            '/bahmni/common/ui-helper/directives/conceptAutocomplete.js',
            '/bahmni/common/ui-helper/directives/focusMe.js',
            '/bahmni/common/ui-helper/directives/bahmniAutocomplete.js',
            '/bahmni/common/ui-helper/services/contextChangeHandler.js',
            '/bahmni/common/ui-helper/controllers/messageController.js',
            '/bahmni/common/ui-helper/printer.js',
            '/bahmni/common/ui-helper/directives/ngConfirmClick.js',
            '/bahmni/common/ui-helper/directives/bmShow.js',
            '/bahmni/common/photo-capture/init.js',
            '/bahmni/common/photo-capture/directives/capturePhoto.js',
            '/bahmni/common/patient/init.js',
            '/bahmni/common/patient/filters/age.js',
            '/bahmni/common/concept-set/init.js',
            '/bahmni/common/concept-set/directives/conceptSetGroup.js',
            '/bahmni/common/concept-set/directives/conceptSet.js',
            '/bahmni/common/concept-set/directives/concept.js',
            '/bahmni/common/concept-set/directives/buttonSelect.js',
            '/bahmni/common/concept-set/directives/stepper.js',
            '/bahmni/common/concept-set/directives/obsConstraints.js',
            '/bahmni/common/concept-set/directives/duration.js',
            '/bahmni/common/concept-set/directives/integer.js',
            '/bahmni/common/concept-set/directives/latestObs.js',
            '/bahmni/common/concept-set/models/conceptSetGroupValidationHandler.js',
            '/bahmni/common/concept-set/models/conceptSetObservation.js',
            '/bahmni/common/concept-set/models/booleanObservation.js',
            '/bahmni/common/concept-set/models/observationNode.js',
            '/bahmni/common/concept-set/models/tabularObservations.js',
            '/bahmni/common/concept-set/models/multiSelectObservations.js',
            '/bahmni/common/concept-set/models/customRepresentationBuilder.js',
            '/bahmni/common/concept-set/models/conceptSetSection.js',
            '/bahmni/common/concept-set/mappers/observationMapper.js',
            '/bahmni/common/concept-set/services/conceptSetServices.js',
            '/bahmni/common/concept-set/services/conceptSetUiConfigService.js',
            '/bahmni/common/ui-helper/filters/thumbnail.js',
            '/bahmni/common/ui-helper/filters/dateFilters.js',
            '/bahmni/common/displaycontrols/init.js',
            '/bahmni/common/displaycontrols/observation/init.js',
            '/bahmni/common/displaycontrols/observation/helpers/groupingFunctions.js',
            '/bahmni/common/displaycontrols/observation/directives/bahmniObservation.js',
            '/bahmni/common/displaycontrols/pivottable/init.js',
            '/bahmni/common/displaycontrols/pivottable/directives/pivotTable.js',
            '/bahmni/common/displaycontrols/pivottable/services/pivotTableService.js',
            '/bahmni/common/displaycontrols/custom/init.js',
            '/bahmni/common/displaycontrols/custom/directives/customDisplayControl.js',
            '/bahmni/common/obs/init.js',
            '/bahmni/common/obs/models/observation.js',
            '/bahmni/common/obs/mappers/observationMapper.js',
            '/bahmni/common/obs/directives/showObservation.js',
            '/bahmni/common/obs/util/observationUtil.js',
            '/bahmni/common/patient-search/constants.js',
            '/bahmni/common/patient-search/models/search.js',
            '/bahmni/common/patient-search/init.js',
            '/bahmni/common/patient-search/controllers/patientsListController.js',
            '/bahmni/common/photo-capture/views/photo.html',
            '/bahmni/common/domain/mappers/attributeFormatter.js',
            '/bahmni/common/domain/mappers/attributeTypeMapper.js',
            '/bahmni/common/attributeTypes/directives/attributeTypes.js',
            '/bahmni/common/attributeTypes/views/attributeInformation.html'
            // end of registration cache
        ],
        preFetchCompleteList = styles.concat(
            preFetchList.concat(
                configs.concat(
                    modules.concat(
                        globalProperty.concat(
                            others.concat(rest))))));

    importScripts('./components/sw-toolbox/sw-toolbox.js');

    //configuration
    global.toolbox.router.default = global.toolbox.networkFirst;
    global.toolbox.options.debug = false;
    global.toolbox.options.cache = {
        name: 'bahmni-home-cache-v-1',
        maxAgeSeconds: null,
        maxEntries: null
    };
    //this should be before any https calls
    global.toolbox.precache(preFetchCompleteList);

    //listeners for life cycle
    global.addEventListener('install', function () {
        console.log('Service worker installed.');
    });
    global.addEventListener('activate', function () {
        console.log('Service worker activated.');
    });

    //routing
    global.toolbox.router.get('/openmrs/(.*)', global.toolbox.networkFirst);
    global.toolbox.router.get('/bahmni_config/(.*)', global.toolbox.networkOnly);
    global.toolbox.router.get('/openmrs/ws/rest/v1/session?v=custom:(uuid)', global.toolbox.networkOnly);
    global.toolbox.router.get('/event-log-service/(.*)', global.toolbox.networkOnly);
    global.toolbox.router.get('/(.*)', global.toolbox.networkFirst);

    //update caches
    var updateCache = function (items) {
        for (var i in items) {
            global.toolbox.uncache(items[i]);
            global.toolbox.cache(items[i]);
        }
    };
    updateCache(configs);
    //updateCache(globalProperty);
    //updateCache(rest);
    //updateCache(preFetchList);
    //updateCache(others);

})(self);
