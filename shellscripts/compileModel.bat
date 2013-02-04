@echo off

if exist "emlabConfig.bat" (
call emlabConfig.bat
) else (
echo Define emlabConfig.bat, by changing the template.
pause
exit
)


set modelhome=%emlabHome%\emlab-model
cd %modelhome%
call mvn clean install
cd %d13nhome%\shellscripts

