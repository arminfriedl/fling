#!/bin/sh

set -xe

mkdir /var/run/nginx # nginx fails with /var/run/nginx/nginx.pid not found in alpine

nginx

cat /var/www/fling/config.js.template | envsubst > /var/www/fling/config.js

java ${FL_JVM_OPTS} -jar service.jar
