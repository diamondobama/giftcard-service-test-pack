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
if [ ! "$CI" = true ]; then
  echo "\$CI not set - assuming this is inside the Heroku build env."
  echo "Not running Hugo when building on Heroku"
  exit 0;
fi
  echo "\$CI is set - assuming this is inside the CircleCI build env."
  echo "Assuming Docker is available for Hugo building."
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

set -e

sh ${SCRIPT_DIR}/hugoPreProcessing.sh $BASE_DIR
sh ${SCRIPT_DIR}/hugoBuild.sh $BASE_DIR
