#!/bin/sh
export MAVEN_OPTS="-d64 -server -Xmx2048m"
mkdir /tmp/ramdisk
sudo mount -t tmpfs -o size=1024M tmpfs /tmp/ramdisk/
