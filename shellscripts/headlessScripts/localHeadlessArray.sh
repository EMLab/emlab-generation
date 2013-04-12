#!/bin/bash

########################################################################
# The jobname must only consist of the characters A-Z, a-z and 0-9!!!! #
########################################################################
USAGE="Provide name of run and number of runs"
#Load configuration script to substitute
if [ -f scriptConfigurations.cfg ];then 
	. scriptConfigurations.cfg
	HOME=$REMOTERESULTFOLDER
else
    echo "Define scriptConfigurations.cfg, by changing the template. Exiting script."
    exit
fi

## the first parameter gives the jobname, the second the scenario-file excluding(!) the xml file-ending
## the scenario files should correspond to the range of number of PBS_ARRAYID.
## Example sh localHeadless.sh example 2 scenarioA
JOBNAME=example
#NROFRUNS=$2
SCENARIO=$3
SCENARIOPATH=file://$LOCALSCENARIOFOLDER
#SCENARIO=scenarioE-MinCO2-resTarget.xml
START=$1
END=$2
for PBS_ARRAYID in {0..2}
do
/usr/lib/jvm/java-1.6.0-openjdk-amd64/bin/java -d64 -server -Xmx3072m -Drun.id=$JOBNAME-$PBS_ARRAYID -DSCENARIO_FOLDER=$SCENARIOPATH -Dresults.path=$LOCALRESULTFOLDER/ -Dscenario.file=$SCENARIO-$PBS_ARRAYID.xml -jar $LOCALFOLDER/target/$JARFILE
echo "$SCENARIO-$PBS_ARRAYID.xml"
rm -rf /tmp/ramdisk/polep-db/$JOBNAME-$PBS_ARRAYID
done
