@echo off

if exist "emlabConfig.bat" (
call emlabConfig.bat
) else (
echo Define emlabConfig.bat, by changing the template.
pause
exit
)

cd %agentspringHome%/agentspring-face/
call mvn jetty:run

