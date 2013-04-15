#!/bin/bash
if [ -f scriptConfigurations.cfg ];then 
	echo "Loading config file!"
        . scriptConfigurations.cfg
	cat scriptConfigurations.cfg
	echo '\n'
	rsync -va --progress $LOCALFOLDER/* $REMOTEUSER@$SERVER:$REMOTEFOLDER
	echo -e '\n\nCopying of EMLAB-generation directory suceeded!'
else
    echo "Define scriptConfigurations.cfg, by changing the template. Exiting script."
fi



