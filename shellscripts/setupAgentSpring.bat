@echo off

if exist "emlabConfig.bat" (
call emlabConfig.bat
) else (
echo Define emlabConfig.bat, by changing the template.
pause
exit
)

cd %agentspringHome%\agentspring-facade
call mvn clean install
cd %agentspringHome%\agentspring-face
call mvn clean install
cd %agentspringHome%\agentspring-engine
call mvn clean install

cd %emlabHome%\shellscripts