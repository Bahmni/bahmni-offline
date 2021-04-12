#!/bin/bash

export LANG=en_US.UTF-8
set -e

echo "in script file"
cd ..
curl -o android.zip  --location --request GET 'https://api.github.com/repos/Bahmni/bahmni-connect/actions/artifacts/44155168/zip' \
-H "Authorization: token $SECRET_AUTH_TOKEN"
ls
unzip -o android.zip
ls
echo "Downloaded the artefact"