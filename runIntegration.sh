#!/bin/sh
source /dev/stdin
$( awk '/^TEST_OPENID/ {print "export", $1}' ./.env )

mvn test -f integration/pom.xml \
    -Dtest.openid.service.url=$TEST_OPENID_TOKEN_SERVICE_URL/ \
    -Dtest.openid.service.auth.username=$TEST_OPENID_TOKEN_SERVICE_AUTH_USERNAME \
    -Dtest.openid.service.auth.password=$TEST_OPENID_TOKEN_SERVICE_AUTH_PASSWORD \
    -Dservice.base.url=http://localhost:6060
    -Dfrontend.notification.base.url=http://localhost:8000