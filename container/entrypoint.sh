#!/bin/sh

set -xe

rc-service nginx start

java ${FL_JVM_OPTS} -jar service.jar
