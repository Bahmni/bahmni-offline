#!/usr/bin/env bash
cp -r ../../../openmrs-module-bahmniapps/ui/app ../www/
mkdir -p ../www/bahmni_config/openmrs/
cp -r ../../../default-config/openmrs/i18n ../www/bahmni_config/openmrs
