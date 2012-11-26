#!/bin/bash
if [ -f emlabConfig.cfg ];then
        . emlabConfig.cfg
else
    echo "Define emlabConfig.cfg, by changing the template. Exiting sc\                                                                    
ript."
    exit
fi
modelhome=$emlabHome/emlab-model

sh $emlabHome/shellscripts/makeRamdisk.sh

#start model
cd $modelhome
mvn exec:java $1
