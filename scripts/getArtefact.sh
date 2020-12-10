#!/bin/bash

export LANG=en_US.UTF-8
set -e

echo "in script file"
curl -o apps.zip --location -H $GIT_AUTH_HEADER https://api.github.com/repos/Bahmni/bahmni-connect/actions/artifacts/29404996/zip
