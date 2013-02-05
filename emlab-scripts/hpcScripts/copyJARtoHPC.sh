#!/bin/bash
if [ -f scriptConfigurations.cfg ];then 
	echo "Loading config file!"
        . scriptConfigurations.cfg
	cat scriptConfigurations.cfg
	echo '\n'
	TARGET=$REMOTERESULTFOLDER
	rsync -va --progress $LOCALD13N/model/trunk/target/$JARFILE $REMOTEUSER@hpc07.tudelft.net:$TARGET
	echo -e '\n\nCopying of JAR file suceeded!'
else
    echo "Define scriptConfigurations.cfg, by changing the template. Exiting script."
fi



