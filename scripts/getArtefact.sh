#!/bin/bash

export LANG=en_US.UTF-8
set -e

echo "in script file"
cd ..
curl -o android.zip  --location --request GET 'https://api.github.com/Rkum113/bahmni-connect/actions/runs/4022218784/zip' \
-H "Authorization: token $SECRET_AUTH_TOKEN"
ls
unzip -o android.zip -d androidDist
ls
echo "Downloaded the artefact"