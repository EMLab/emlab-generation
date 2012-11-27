#!/bin/bash
if [ -f emlabConfig.cfg ];then
        . emlabConfig.cfg
else
    echo "Define emlabConfig.cfg, by changing the template. Exiting sc\                                                                    
ript."
    exit
fi

#download agentspring if it is not there
git clone https://github.com/alfredas/AgentSpring.git $agentSpringHome

#update agentspring if it is there
#cd $agentSpringHome
#git pull

#compile and install agentspring
cd $agentSpringHome/
mvn clean install $1

