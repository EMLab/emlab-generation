#!/bin/bash
agentspringhome=~/AgentSpring

#compile and install agentspring
cd $agentspringhome/agentspring-facade/
mvn clean install $1
cd $agentspringhome/agentspring-face/
mvn clean install $1
cd $agentspringhome/agentspring-engine/
mvn clean install $1
