#!/bin/bash
basedir=$1
testdir=$2
echo "Starting docker container"
docker build -t="giftcard-service-test-pack" ${basedir}/target
docker run -d -p 8080:8080 --name giftcard-service-test-pack_container giftcard-service-test-pack
/git/circlecitools/bin/waitForServer.sh localhost:8080 5000
${testdir}/run_newman.sh ${testdir}
rc=$?
echo "Cleaning up Docker"
docker stop giftcard-service-test-pack_container
docker rm giftcard-service-test-pack_container
docker rmi giftcard-service-test-pack
exit $rc
