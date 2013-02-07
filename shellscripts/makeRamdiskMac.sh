#!/bin/bash
export MAVEN_OPTS="-d64 -server -Xmx2048m"
diskutil erasevolume HFS+ "ramdisk" `hdiutil attach -nomount ram://1165430`
ln -s /Volumes/ramdisk /tmp/ramdisk
