#!/bin/bash
if [ -f scriptConfigurations.cfg ];then 
	echo "Loading config file!"
        . scriptConfigurations.cfg
	cat scriptConfigurations.cfg
	echo '\n'
	rsync -va --progress $LOCALD13N/* $REMOTEUSER@hpc07.tudelft.net:$REMOTED13N
	echo -e '\n\nCopying of EMLAB directory suceeded!'
else
    echo "Define scriptConfigurations.cfg, by changing the template. Exiting script."
fi



