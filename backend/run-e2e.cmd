@echo off
setlocal

set "TEST_TARGET=%~1"
if "%TEST_TARGET%"=="" set "TEST_TARGET="

set "BASE_URL=%~2"
if "%BASE_URL%"=="" set "BASE_URL=http://localhost:5173"

set "HEADLESS=%~3"
if "%HEADLESS%"=="" set "HEADLESS=false"

if "%TEST_TARGET%"=="" (
	call "%~dp0mvnw.cmd" -Pe2e "-Dselenium.baseUrl=%BASE_URL%" "-Dselenium.headless=%HEADLESS%" failsafe:integration-test failsafe:verify
) else (
	call "%~dp0mvnw.cmd" -Pe2e "-Dselenium.baseUrl=%BASE_URL%" "-Dselenium.headless=%HEADLESS%" "-Dit.test=%~n1" failsafe:integration-test failsafe:verify
)

endlocal