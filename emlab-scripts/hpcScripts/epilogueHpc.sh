#!/bin/bash

# Make sure that permissions to this file and the sourrounding directory
# are set with chmod go+x

# we get a directory name from the current job array id, and set other variables
dir=$4

TEMP=/var/tmp
D13NDB=d13n-db
RAMDISK=/tmp/ramdisk
#Empty ramdisk
rm -rf $RAMDISK/$D13NDB/$dir

#delete all the data and the folder from the node
rm -rf $TEMP/$dir

#remove directories (rmdir only works if empty)
rmdir $RAMDISK/$D13NDB
rmdir $RAMDISK

echo "Epilogue&Cleaning done."