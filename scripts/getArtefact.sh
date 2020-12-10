#!/bin/bash

export LANG=en_US.UTF-8
set -e

echo "in script file"
curl -o apps.zip --location -H "Authorization: token 878cdb2814740a370ac12b4fad411e96415cdcb1" https://api.github.com/repos/Bahmni/bahmni-connect/actions/artifacts/29404996/zip
