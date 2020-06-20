#!/bin/sh

set -xe

mkdir /var/run/nginx # nginx fails with /var/run/nginx/nginx.pid not found in alpine

nginx

java ${FL_JVM_OPTS} -jar service.jar
