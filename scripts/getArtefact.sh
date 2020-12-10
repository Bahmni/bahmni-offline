#!/bin/bash

export LANG=en_US.UTF-8
set -e

echo "in script file"
cd ..
curl -o android.zip  --location --request GET 'https://api.github.com/repos/Bahmni/bahmni-connect/actions/artifacts/29404996/zip' \
--header "$GIT_AUTH_HEADER"
ls
unzip -o android.zip
ls
echo "Downloaded the artefact"