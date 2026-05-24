# Minimal IExpress packager: compiles Java, creates JAR, builds IExpress installer into ./Release
$ErrorActionPreference = 'Stop'
$ProjectDir = (Get-Location).Path
$TempDir = Join-Path $env:TEMP ("NotepadPackage_{0}" -f (Get-Random))
if (-not (Test-Path (Join-Path $ProjectDir 'bin'))) { New-Item -ItemType Directory -Path (Join-Path $ProjectDir 'bin') | Out-Null }

# Detect javac & jar
$javac = (Get-Command javac -ErrorAction SilentlyContinue).Source
if (-not $javac) { Write-Error "javac not found in PATH"; exit 1 }
$jar = (Get-Command jar -ErrorAction SilentlyContinue).Source
if (-not $jar) {
    $jdkBin = Split-Path -Parent $javac
    $altJar = Join-Path $jdkBin 'jar.exe'
    if (Test-Path $altJar) {
        $jar = $altJar
    }
}
if (-not $jar) {
    $javaHome = $env:JAVA_HOME
    if (-not $javaHome) {
        $javaHome = Get-ChildItem -Path 'C:\Program Files\Java' -Directory -ErrorAction SilentlyContinue |
            Where-Object Name -Like 'jdk*' |
            Sort-Object Name -Descending |
            Select-Object -First 1 -ExpandProperty FullName
    }
    if (-not $javaHome) {
        $javaHome = Get-ChildItem -Path 'C:\Program Files (x86)\Java' -Directory -ErrorAction SilentlyContinue |
            Where-Object Name -Like 'jdk*' |
            Sort-Object Name -Descending |
            Select-Object -First 1 -ExpandProperty FullName
    }
    if ($javaHome) {
        $javaHomeJar = Join-Path $javaHome 'bin\jar.exe'
        if (Test-Path $javaHomeJar) {
            $jar = $javaHomeJar
        }
    }
}
if (-not $jar) { Write-Error "jar not found in PATH, alongside javac, or under JAVA_HOME/java.home"; exit 1 }

# Find main
$mainFile = Get-ChildItem -Path $ProjectDir -Recurse -Filter *.java | Select-String -Pattern 'public\s+static\s+void\s+main\s*\(' | Select-Object -First 1
if (-not $mainFile) { Write-Error "No main() found"; exit 1 }
$MainClass = [System.IO.Path]::GetFileNameWithoutExtension($mainFile.Path)
$ProjectName = $MainClass

# Compile
$sources = Get-ChildItem -Path $ProjectDir -Recurse -Filter *.java | Select-Object -ExpandProperty FullName
& $javac -d (Join-Path $ProjectDir 'bin') @($sources)
if ($LASTEXITCODE -ne 0) { Write-Error "Compilation failed"; exit 1 }

# Manifest
$manifest = "Main-Class: $MainClass`n"
Set-Content -Path (Join-Path $ProjectDir 'manifest.txt') -Value $manifest -NoNewline

# Build jar
& $jar cfm (Join-Path $ProjectDir ("$ProjectName.jar")) (Join-Path $ProjectDir 'manifest.txt') -C (Join-Path $ProjectDir 'bin') .
if (-not (Test-Path (Join-Path $ProjectDir ("$ProjectName.jar")))) { Write-Error "Jar not created"; exit 1 }

# Prepare Release and Temp
$ReleaseDir = Join-Path $ProjectDir 'Release'
if (-not (Test-Path $ReleaseDir)) { New-Item -ItemType Directory -Path $ReleaseDir | Out-Null }
New-Item -ItemType Directory -Path $TempDir | Out-Null
Copy-Item -Path (Join-Path $ProjectDir ("$ProjectName.jar")) -Destination (Join-Path $TempDir ("$ProjectName.jar")) -Force
Copy-Item -Path (Join-Path $ProjectDir ("$ProjectName.jar")) -Destination (Join-Path $ReleaseDir ("$ProjectName.jar")) -Force

# Create run.bat using a template to avoid quoting issues
$runTemplate = @'
@echo off
java -jar "%~dp0\__JAR__"
'@
$runContent = $runTemplate -replace '__JAR__', "$ProjectName.jar"
Set-Content -Path (Join-Path $TempDir 'run.bat') -Value $runContent -Encoding ASCII

# Create SED template (single-quoted to avoid expansion)
$sedTemplate = @'
[Version]
Class=IEXPRESS
SEDVersion=3

[Options]
PackagePurpose=InstallApp
ShowInstallProgramWindow=1
HideExtractAnimation=0
UseLongFileNames=1
InsideCAB=0
CABFiles=0
TargetName=__TARGET__
FriendlyName=Setup __NAME__
InstallPrompt=
DisplayName=Setup __NAME__
InstallProgram=run.bat
AppLaunched=run.bat
PostInstallCmd=

[SourceFiles]
SourceFiles0=__TEMPDIR__

[SourceFiles0]
run.bat=
__JAR__=
'@

$targetName = "Setup_$ProjectName.exe"
$targetExe = Join-Path $ReleaseDir $targetName
$sed = $sedTemplate -replace '__TARGET__', $targetName -replace '__NAME__', $ProjectName -replace '__TEMPDIR__', $TempDir -replace '__JAR__', "$ProjectName.jar"
$sedPath = Join-Path $TempDir 'package.sed'
Set-Content -Path $sedPath -Value $sed -Encoding ASCII

# Build with IExpress
$iexpress = Join-Path $env:WINDIR 'system32\iexpress.exe'
if (-not (Test-Path $iexpress)) { Write-Error "IExpress not found on this system"; exit 1 }
Write-Host "Running IExpress to build installer..."
Push-Location $ReleaseDir
& $iexpress '/N' '/Q' $sedPath
Pop-Location

if (-not (Test-Path $targetExe)) { Write-Error "IExpress did not produce installer at $targetExe"; exit 1 }
Write-Host "Installer created at: $targetExe"

# cleanup
Remove-Item -Recurse -Force $TempDir -ErrorAction SilentlyContinue
Remove-Item -Force (Join-Path $ProjectDir 'manifest.txt') -ErrorAction SilentlyContinue
