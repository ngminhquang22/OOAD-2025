@echo off
echo Compiling Java Project...

if not exist "bin" mkdir bin

javac -d bin -sourcepath src\main\java src\main\java\com\gaminglounge\Main.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b %errorlevel%
)

echo Compilation successful!
echo Running Application...
echo NOTE: If you see "No suitable driver found", you need to add the MySQL Connector JAR to the classpath.
echo.

java -cp "bin;lib\mysql-connector-j-8.0.33.jar" com.gaminglounge.Main

pause
