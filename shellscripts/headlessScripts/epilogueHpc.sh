#!/bin/bash

# Make sure that permissions to this file and the sourrounding directory
# are set with chmod go+x

# we get a directory name from the current job array id, and set other variables
dir=$4

echo $dir

TEMP=/var/tmp
EMLABDB=emlab.gen-db
RAMDISK=/tmp/ramdisk
#Empty ramdisk
rm -rf $RAMDISK/$EMLABDB/$dir

#delete all the data and the folder from the node
rm -rf $TEMP/$dir

#remove directories (rmdir only works if empty)
rmdir $RAMDISK/$EMLABDB
rmdir $RAMDISK

echo "Epilogue&Cleaning done."