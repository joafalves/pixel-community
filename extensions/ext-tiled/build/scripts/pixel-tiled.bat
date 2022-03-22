@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  pixel-tiled startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and PIXEL_TILED_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\pixel-tiled-0.5.0.jar;%APP_HOME%\lib\pixel-core-0.5.0.jar;%APP_HOME%\lib\pixel-commons-0.5.0.jar;%APP_HOME%\lib\pixel-input-0.5.0.jar;%APP_HOME%\lib\pixel-math-0.5.0.jar;%APP_HOME%\lib\lwjgl-glfw-3.2.3.jar;%APP_HOME%\lib\lwjgl-glfw-3.2.3-natives-macos.jar;%APP_HOME%\lib\lwjgl-glfw-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-glfw-3.2.3-natives-linux.jar;%APP_HOME%\lib\lwjgl-opengl-3.2.3.jar;%APP_HOME%\lib\lwjgl-opengl-3.2.3-natives-macos.jar;%APP_HOME%\lib\lwjgl-opengl-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-opengl-3.2.3-natives-linux.jar;%APP_HOME%\lib\lwjgl-openal-3.2.3.jar;%APP_HOME%\lib\lwjgl-openal-3.2.3-natives-macos.jar;%APP_HOME%\lib\lwjgl-openal-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-openal-3.2.3-natives-linux.jar;%APP_HOME%\lib\lwjgl-stb-3.2.3.jar;%APP_HOME%\lib\lwjgl-stb-3.2.3-natives-macos.jar;%APP_HOME%\lib\lwjgl-stb-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-stb-3.2.3-natives-linux.jar;%APP_HOME%\lib\lwjgl-nanovg-3.2.3.jar;%APP_HOME%\lib\lwjgl-nanovg-3.2.3-natives-macos.jar;%APP_HOME%\lib\lwjgl-nanovg-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-nanovg-3.2.3-natives-linux.jar;%APP_HOME%\lib\lwjgl-yoga-3.2.3.jar;%APP_HOME%\lib\lwjgl-yoga-3.2.3-natives-macos.jar;%APP_HOME%\lib\lwjgl-yoga-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-yoga-3.2.3-natives-linux.jar;%APP_HOME%\lib\lwjgl-3.2.3.jar;%APP_HOME%\lib\lwjgl-3.2.3-natives-macos.jar;%APP_HOME%\lib\lwjgl-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-3.2.3-natives-linux.jar;%APP_HOME%\lib\junit-jupiter-params-5.7.0.jar;%APP_HOME%\lib\junit-jupiter-engine-5.7.0.jar;%APP_HOME%\lib\junit-jupiter-api-5.7.0.jar;%APP_HOME%\lib\junit-platform-engine-1.7.0.jar;%APP_HOME%\lib\junit-platform-commons-1.7.0.jar;%APP_HOME%\lib\junit-jupiter-5.7.0.jar;%APP_HOME%\lib\mockito-core-3.12.4.jar;%APP_HOME%\lib\json-20190722.jar;%APP_HOME%\lib\byte-buddy-1.11.13.jar;%APP_HOME%\lib\byte-buddy-agent-1.11.13.jar;%APP_HOME%\lib\objenesis-3.2.jar;%APP_HOME%\lib\apiguardian-api-1.1.0.jar;%APP_HOME%\lib\opentest4j-1.2.0.jar


@rem Execute pixel-tiled
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %PIXEL_TILED_OPTS%  -classpath "%CLASSPATH%"  %*

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable PIXEL_TILED_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%PIXEL_TILED_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
