#!/bin/bash
if [ -f emlabConfig.cfg ];then
        . emlabConfig.cfg
else
    echo "Define emlabConfig.cfg, by changing the template. Exiting sc\                                                                    
ript."
    exit
fi

#start model
cd $emlabModelFolder
mvn clean install -DskipTests "$@"
