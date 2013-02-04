@echo off

if exist "emlabConfig.bat" (
call emlabConfig.bat
) else (
echo Define emlabConfig.bat, by changing the template.
pause
exit
)

cd %emlabHome%\emlab-model
call mvn exec:java
cd %emlabHome%\shellscripts
