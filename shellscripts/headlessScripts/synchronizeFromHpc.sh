#!/bin/bash
if [ -f scriptConfigurations.cfg ];then 
	echo "Loading config file!"
        . scriptConfigurations.cfg
	cat scriptConfigurations.cfg
	TARGET=$LOCALRESULTFOLDER
	SOURCE=$REMOTERESULTFOLDER/*
	rsync -va --progress -r $REMOTEUSER@$SERVER:$SOURCE $TARGET
	echo -e "\n\nFinished synchronisation from HPC!"
else
    echo "Define scriptConfigurations.cfg, by changing the template. Exiting script."
fi


