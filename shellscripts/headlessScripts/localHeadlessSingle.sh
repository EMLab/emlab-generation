#!/bin/bash

########################################################################
# The jobname must only consist of the characters A-Z, a-z and 0-9!!!! #
########################################################################
USAGE="Provide name of run and name of scenario file."
#Load configuration script to substitute
if [ -f scriptConfigurations.cfg ];then 
	. scriptConfigurations.cfg
	HOME=$REMOTERESULTFOLDER
else
    echo "Define scriptConfigurations.cfg, by changing the template. Exiting script."
    exit
fi

## the first parameter gives the jobname, the second the scenario-file including the xml file-ending
## Example sh localHeadless.sh example scenarioA-ToyModel.xml
JOBNAME=$1
SCENARIO=$2
SCENARIOPATH=file://$LOCALSCENARIOFOLDER

mkdir $LOCALRESULTFOLDER/$JOBNAME
cd $LOCALRESULTFOLDER/$JOBNAME
if [ ! -z $3 ] 
then 
    QUERYCOMMAND="-Dquery.file=$3"
else
    QUERYCOMMAND=""
fi

java -d64 -server -Xmx3072m -Drun.id=$JOBNAME -DSCENARIO_FOLDER=$SCENARIOPATH -Dresults.path=$LOCALRESULTFOLDER/$JOBNAME -Dscenario.file=$SCENARIO".xml" $QUERYCOMMAND -jar $LOCALJARFILE >  $JOBNAME.log
rm -rf /tmp/ramdisk/emlab.gen-db/$JOBNAME

#mv simulation.log $JOBNAME.log
