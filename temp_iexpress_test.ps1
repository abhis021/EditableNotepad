$testDir = Join-Path $env:TEMP 'iexpress_test2'
Remove-Item -Recurse -Force $testDir -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path $testDir | Out-Null
Set-Content -Path (Join-Path $testDir 'run.bat') -Value '@echo off`r`necho hello' -Encoding ASCII

$sed = @'
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
TargetName=TestInstaller.exe
FriendlyName=Test Installer
InstallPrompt=
DisplayName=Test Installer
AppLaunched=run.bat
PostInstallCmd=

[SourceFiles]
SourceFiles0=__TEMP__

[SourceFiles0]
run.bat=
'@ -replace '__TEMP__', $testDir
Set-Content -Path (Join-Path $testDir 'package.sed') -Value $sed -Encoding ASCII

$iexpress = Join-Path $env:WINDIR 'system32\iexpress.exe'
Push-Location $testDir
& $iexpress '/N' '/Q' (Join-Path $testDir 'package.sed')
$exit = $LASTEXITCODE
Pop-Location
Write-Host "EXIT=$exit"
Get-ChildItem -Path $testDir -Filter *.exe -Recurse | Select-Object FullName,Length | Format-Table -AutoSize
