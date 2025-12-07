@echo off
setlocal enabledelayedexpansion

:: make.bat - Windows build script for java-system-hotkey
:: Usage: make [target]
:: Targets: all, java, jai, jni, example, jar, clean, clean-all, help

set "BUILD_DIR=build"
set "CLASSES_DIR=%BUILD_DIR%\classes"
set "NATIVES_DIR=src\main\resources\natives\windows-x86_64"
set "JAI_SRC_DIR=jai-system-hotkey"

:: Auto-detect JAVA_HOME if not set
if not defined JAVA_HOME (
    for /f "delims=" %%i in ('where java 2^>nul') do (
        set "JAVA_PATH=%%~dpi"
        for %%j in ("!JAVA_PATH!..") do set "JAVA_HOME=%%~fj"
        goto :found_java
    )
    echo Error: JAVA_HOME not set and java not found in PATH
    exit /b 1
)
:found_java

:: Find GCC - check common locations
set "GCC_EXE="
where gcc >nul 2>&1 && set "GCC_EXE=gcc"
if not defined GCC_EXE (
    :: Check WinGet installation
    for /f "delims=" %%i in ('dir /s /b "%LOCALAPPDATA%\Microsoft\WinGet\Packages\*gcc.exe" 2^>nul ^| findstr mingw64') do (
        set "GCC_EXE=%%i"
        goto :found_gcc
    )
)
:found_gcc

if "%1"=="" goto :all
if "%1"=="all" goto :all
if "%1"=="java" goto :java
if "%1"=="jai" goto :jai
if "%1"=="jni" goto :jni
if "%1"=="example" goto :example
if "%1"=="jar" goto :jar
if "%1"=="clean" goto :clean
if "%1"=="clean-all" goto :clean-all
if "%1"=="help" goto :help
echo Unknown target: %1
goto :help

:all
call :java
if errorlevel 1 exit /b 1
call :jni
exit /b %errorlevel%

:java
echo Compiling Java sources...
if not exist "%CLASSES_DIR%" mkdir "%CLASSES_DIR%"
javac -d "%CLASSES_DIR%" src\main\java\app\uncrumpled\systemhotkey\*.java
if errorlevel 1 (
    echo Error: Java compilation failed
    exit /b 1
)
echo Java compilation complete.
exit /b 0

:jai
echo Building Jai shared library...
if not exist "%NATIVES_DIR%" mkdir "%NATIVES_DIR%"

:: Check if already built
if exist "%JAI_SRC_DIR%\lib\system_hotkey.dll" (
    echo Copying Jai library...
    copy /Y "%JAI_SRC_DIR%\lib\system_hotkey.dll" "%NATIVES_DIR%\" >nul
    echo Jai library copied.
    exit /b 0
)

:: Build it
pushd "%JAI_SRC_DIR%"
jai build_shared.jai
if errorlevel 1 (
    popd
    echo Error: Jai build failed
    exit /b 1
)
popd

if exist "%JAI_SRC_DIR%\lib\system_hotkey.dll" (
    copy /Y "%JAI_SRC_DIR%\lib\system_hotkey.dll" "%NATIVES_DIR%\" >nul
    echo Jai library built and copied.
) else (
    echo Error: Jai library not found after build
    exit /b 1
)
exit /b 0

:jni
:: Ensure jai library exists first
if not exist "%NATIVES_DIR%\system_hotkey.dll" (
    call :jai
    if errorlevel 1 exit /b 1
)

echo Building JNI library...
if not exist "%NATIVES_DIR%" mkdir "%NATIVES_DIR%"

if not defined GCC_EXE (
    echo Error: GCC not found. Install MinGW-w64:
    echo   winget install BrechtSanders.WinLibs.POSIX.UCRT
    exit /b 1
)

"%GCC_EXE%" -shared ^
    -I"%JAVA_HOME%\include" ^
    -I"%JAVA_HOME%\include\win32" ^
    -I"%NATIVES_DIR%" ^
    -o "%NATIVES_DIR%\system_hotkey_jni.dll" ^
    src\main\c\system_hotkey_jni.c ^
    -L"%NATIVES_DIR%" -lsystem_hotkey

if errorlevel 1 (
    echo Error: JNI compilation failed
    exit /b 1
)
echo JNI library built.
exit /b 0

:example
call :java
if errorlevel 1 exit /b 1
call :jni
if errorlevel 1 exit /b 1

echo Compiling example...
javac -cp "%CLASSES_DIR%" -d "%CLASSES_DIR%" examples\Example.java
if errorlevel 1 (
    echo Error: Example compilation failed
    exit /b 1
)

echo.
echo Running example...
echo.
java -cp "%CLASSES_DIR%" --enable-native-access=ALL-UNNAMED -Djava.library.path="%CD%\%NATIVES_DIR%" Example
exit /b %errorlevel%

:jar
call :java
if errorlevel 1 exit /b 1
echo Creating JAR...
if not exist "%BUILD_DIR%" mkdir "%BUILD_DIR%"
jar cf "%BUILD_DIR%\system-hotkey.jar" -C "%CLASSES_DIR%" .
echo JAR created: %BUILD_DIR%\system-hotkey.jar
exit /b 0

:clean
echo Cleaning build artifacts...
if exist "%BUILD_DIR%" rmdir /s /q "%BUILD_DIR%"
if exist "%NATIVES_DIR%\system_hotkey_jni.dll" del /q "%NATIVES_DIR%\system_hotkey_jni.dll"
echo Clean complete.
exit /b 0

:clean-all
call :clean
echo Cleaning all natives...
if exist "src\main\resources\natives" rmdir /s /q "src\main\resources\natives"
echo Clean-all complete.
exit /b 0

:help
echo.
echo Usage: make [target]
echo.
echo Targets:
echo   all        - Build Java and JNI (default)
echo   java       - Compile Java sources
echo   jai        - Build/copy Jai library
echo   jni        - Build JNI native library
echo   example    - Build and run the example program
echo   jar        - Create JAR file
echo   clean      - Clean build artifacts
echo   clean-all  - Clean everything including natives
echo   help       - Show this help
echo.
echo Build the Jai library first (if not auto-built):
echo   cd %JAI_SRC_DIR% ^&^& jai build_shared.jai
echo.
exit /b 0
