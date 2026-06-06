# Bargain Here - Self-Contained Maven Build and Run Script
# This script ensures Maven is available locally (downloading a portable version if needed) 
# and compiles/runs the Spring Boot application under Java 8.

$ErrorActionPreference = "Stop"
$env:_JAVA_OPTIONS = "-Djava.net.preferIPv4Stack=true"
$env:JAVA_HOME = "C:\Program Files\Java\jdk1.8.0_181"
$env:Path = "C:\Program Files\Java\jdk1.8.0_181\bin;" + $env:Path

$ProjectDir = $PSScriptRoot
if ([string]::IsNullOrEmpty($ProjectDir)) {
    $ProjectDir = Get-Location
}

$MavenDir = Join-Path $ProjectDir ".maven"
$MavenZip = Join-Path $ProjectDir "maven-portable.zip"
$LocalMvnCmd = Join-Path $MavenDir "apache-maven-3.8.8\bin\mvn.cmd"

# 1. Determine Maven command to use
$MvnPath = ""
if (Get-Command "mvn" -ErrorAction SilentlyContinue) {
    Write-Host "[Bargain Build] Found system Maven installation." -ForegroundColor Green
    $MvnPath = "mvn"
} elseif (Test-Path $LocalMvnCmd) {
    Write-Host "[Bargain Build] Found portable Maven installation in: $LocalMvnCmd" -ForegroundColor Green
    $MvnPath = $LocalMvnCmd
} else {
    Write-Host "[Bargain Build] System Maven not found. Downloading portable Apache Maven 3.8.8..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Force -Path $MavenDir | Out-Null
    
    $DownloadUrl = "https://archive.apache.org/dist/maven/maven-3/3.8.8/binaries/apache-maven-3.8.8-bin.zip"
    Write-Host "[Bargain Build] Fetching $DownloadUrl ..." -ForegroundColor Cyan
    
    if (Get-Command "curl.exe" -ErrorAction SilentlyContinue) {
        curl.exe -k --ssl-no-revoke -L -o $MavenZip $DownloadUrl
    } else {
        $client = New-Object System.Net.WebClient
        $client.DownloadFile($DownloadUrl, $MavenZip)
    }
    
    Write-Host "[Bargain Build] Extracting Maven binaries..." -ForegroundColor Cyan
    Expand-Archive -Path $MavenZip -DestinationPath $MavenDir -Force
    
    Write-Host "[Bargain Build] Cleaning up zip download..." -ForegroundColor Cyan
    Remove-Item -Path $MavenZip -Force -ErrorAction SilentlyContinue
    
    if (Test-Path $LocalMvnCmd) {
        Write-Host "[Bargain Build] Portable Maven installed successfully!" -ForegroundColor Green
        $MvnPath = $LocalMvnCmd
    } else {
        throw "Failed to verify local Maven installation at: $LocalMvnCmd"
    }
}

# 2. Parse arguments to decide action
$Action = "compile"
if ($args.Count -gt 0) {
    $Action = $args[0].ToLower()
}

# 3. Perform actions
if ($Action -eq "run") {
    Write-Host "[Bargain Build] Booting Spring Boot App..." -ForegroundColor Green
    & $MvnPath spring-boot:run
} elseif ($Action -eq "build") {
    Write-Host "[Bargain Build] Packaging application JAR..." -ForegroundColor Green
    & $MvnPath clean package
} else {
    Write-Host "[Bargain Build] Compiling classes..." -ForegroundColor Green
    & $MvnPath clean compile
}
