#!/bin/bash
if [ -f emlabConfig.cfg ];then
        . emlabConfig.cfg
else
    echo "Define emlabConfig.cfg, by changing the template. Exiting sc\                                                                    
ript."
    exit
fi

modelhome=$emlabHome/emlab-model

#start model
cd $modelhome
mvn clean install $1
