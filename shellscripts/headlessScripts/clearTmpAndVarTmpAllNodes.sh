#!/bin/bash
if [ -f scriptConfigurations.cfg ];then 
	. scriptConfigurations.cfg
	HOME=$REMOTEHOME
else
    echo "Define scriptConfigurations.cfg, by changing the template. Exiting script."
    exit
fi
for i in {1..60}
do
   qsub -lnodes=n07-`printf "%02d\n" $i` $REMOTEHPCSCRIPTS/singleNodeTmpAndVarClearing.sh
done
