@ECHO OFF
SETLOCAL

SET DIRNAME=%~dp0
IF "%DIRNAME%" == "" SET DIRNAME=.
SET APP_BASE_NAME=%~n0
SET APP_HOME=%DIRNAME%

SET DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

IF NOT "%JAVA_HOME%" == "" GOTO FindJavaHome

SET JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
IF "%ERRORLEVEL%" == "0" GOTO Execute

ECHO ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
EXIT /B 1

:FindJavaHome
SET JAVA_EXE=%JAVA_HOME%/bin/java.exe

IF EXIST "%JAVA_EXE%" GOTO Execute

ECHO ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
EXIT /B 1

:Execute
SET CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% ^
  -Dorg.gradle.appname=%APP_BASE_NAME% ^
  -classpath "%CLASSPATH%" ^
  org.gradle.wrapper.GradleWrapperMain %*

ENDLOCAL
