#!/bin/bash
diskutil erasevolume HFS+ "ramdisk" `hdiutil attach -nomount ram://1165430`
ln -s /Volumes/ramdisk /tmp/ramdisk
