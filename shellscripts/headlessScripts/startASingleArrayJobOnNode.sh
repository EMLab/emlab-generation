#!/bin/bash

# the job number
# we get a directory name from the current job array id
dir=$JOBNAME-$PBS_ARRAYID
NEWJARNAME=$JOBNAME".jar"
#delete database of possible previous run
#rm -rf $RAMDISK/$EMLABDB/$dir
rm -rf $RAMDISK/$EMLABDB/$dir
mkdir -p $RAMDISK/$EMLABDB
chmod go+rxw $RAMDISK/
chmod go+rxw $RAMDISK/$EMLABDB

#cd /var/tmp

# make the directory, and make sure its empty
mkdir $TEMP/$dir
rm -rf $TEMP/$dir/*
NODESCENARIOFOLDER=$TEMP/$dir/inputParameter
mkdir $NODESCENARIOFOLDER
#Is important, since it determines where the log file is saved.
cd $TEMP/$dir

cp $INPUTPARAMETERFOLDER/$SCENARIO $NODESCENARIOFOLDER
cp $INPUTPARAMETERFOLDER/$PARAMETERFILE $NODESCENARIOFOLDER

#FIXME should also test presence of other simulation files and if they match in md5
if [ -e $TEMP/$JARNAME ]; 
then
	#check if it is the same md5 	
	testMD=`(md5sum $TEMP/$JARNAME | sed 's/ /_/g')`
	if [ $testMD != $MD ];
	then
	#if the MD5 sum is not the same as the one we started the main script with, copy it here
	cp $PBS_O_WORKDIR/$JARNAME $TEMP/
	mv $TEMP/$JARNAME $TEMP/$NEWJARNAME
	fi
elif [ -e $TEMP/$NEWJARNAME ];then
	echo "check if it is the same md5"
	testMD=`(md5sum $TEMP/$NEWJARNAME | sed 's/ /_/g')`
	if [ $testMD != $MD ];
	then
	#if the MD5 sum is not the same as the one we started the main script with, copy it here
	cp $PBS_O_WORKDIR/$JARNAME $TEMP/
	mv $TEMP/$JARNAME $TEMP/$NEWJARNAME
	echo "Copying over new JAR file."
	fi
else
#if the jar is not here, copy it over
cp $PBS_O_WORKDIR/$JARNAME $TEMP/
mv $TEMP/$JARNAME $TEMP/$NEWJARNAME
#cp $PBS_O_WORKDIR/$SCENARIO $TEMP/$dir
fi

RAMDU=$(du -sh $RAMDISK)

echo "Disk usage on Ramdisk: $RAMDU"

#Execute the job 
#java -Drun.id=$JOBNAME-$PBS_ARRAYID -Dresults.path=$TEMP/$dir -Dscenario.file=$SCENARIO -jar $PBS_O_WORKDIR/$JARNAME
java -d64 -server -Xmx3072m -Drun.id=$JOBNAME-$PBS_ARRAYID -DSCENARIO_FOLDER=file://$NODESCENARIOFOLDER -Dresults.path=$TEMP/$dir -Dscenario.file=$SCENARIO -jar $TEMP/$NEWJARNAME > $JOBNAME-$PBS_ARRAYID.elog


#REMOVE JAR File
#rm -f $TEMP/$dir/$JARNAME

#Uncomment if only succesful attempts should be copied
#if grep -Fq "WARN Stopping AgentSpring" $JOBNAME-$PBS_ARRAYID.elog
#then
#Rename simulation.log
mv simulation.log $JOBNAME-$PBS_ARRAYID.log
#Make a dir for the data and copy it to that directory so we can access the data from the head node
cp -r $TEMP/$dir/* $PBS_O_WORKDIR/$RUNNAME/
#fi

#Delete folder and ramdisk.
rm -rf $TEMP/$dir
rm -rf $RAMDISK/$EMLABDB/$dir
