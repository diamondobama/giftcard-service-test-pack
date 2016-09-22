#!/usr/bin/env bash

# Run a hugo build from the comfort of a docker container
#
# ----------------------------------------------------------------------------------------------------------------------
# usage:
#
#   hugoBuild.sh BASE_DIR
#
#     BASE_DIR: maven ${basedir} property, the base dir of this project
#


BASE_DIR=$1
if [ ! -d "$BASE_DIR/../circlecitools" ]; then
  echo "Unable to locate circlecitools repo - assuming this is inside the Heroku build env."
  echo "Not running Hugo when building on Heroku"
  exit 0;
fi
  echo "Located circlecitools repo - assuming this is not inside the Heroku build env."
  echo "Assuming Docker is available for Hugo building."
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

set -e

sh ${SCRIPT_DIR}/hugoPreProcessing.sh $BASE_DIR
sh ${SCRIPT_DIR}/hugoBuild.sh $BASE_DIR
