$projectRoot = "C:\vh\oop\OOAD-2025\java_project"
Set-Location $projectRoot

# Check for MySQL Driver
$localLibJar = "$projectRoot\lib\mysql-connector-j-8.0.33.jar"
$mavenJar = "$env:USERPROFILE\.m2\repository\mysql\mysql-connector-java\8.0.33\mysql-connector-java-8.0.33.jar"
$classpath = "bin"

# Add all jars in lib to classpath
$libJars = Get-ChildItem "$projectRoot\lib\*.jar"
foreach ($jar in $libJars) {
    $classpath += ";$($jar.FullName)"
}

if (Test-Path $localLibJar) {
    Write-Host "Found MySQL Driver in local lib." -ForegroundColor Green
} elseif (Test-Path $mavenJar) {
    $classpath += ";$mavenJar"
    Write-Host "Found MySQL Driver in Maven repo." -ForegroundColor Green
} else {
    Write-Host "Warning: MySQL Driver not found! Database connection will fail." -ForegroundColor Red
}

if (Test-Path "bin") {
    Remove-Item -Path "bin" -Recurse -Force
}
New-Item -ItemType Directory -Path "bin" | Out-Null

Write-Host "Compiling..." -ForegroundColor Cyan
# Need to include classpath during compilation too
javac -cp $classpath -d bin -sourcepath src/main/java src/main/java/com/gaminglounge/Main.java

if ($LASTEXITCODE -eq 0) {
    Write-Host "Running..." -ForegroundColor Green
    java -cp $classpath com.gaminglounge.Main
} else {
    Write-Host "Compilation Failed!" -ForegroundColor Red
}
