#!/bin/bash
mkdir /tmp/ramdisk
sudo mount -t tmpfs -o size=512M tmpfs /tmp/ramdisk/
