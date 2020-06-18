#!/bin/sh

set -xe

nginx

java ${FL_JVM_OPTS} -jar service.jar
