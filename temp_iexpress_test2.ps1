$testDir = 'C:\temp_iexpress_test'
Remove-Item -Recurse -Force $testDir -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path $testDir | Out-Null
Set-Content -Path (Join-Path $testDir 'run.bat') -Value '@echo off`r`necho hello' -Encoding ASCII

$sed = @'
[Version]
Class=IEXPRESS
SEDVersion=3

[Options]
PackagePurpose=ExtractFiles
ShowInstallProgramWindow=1
HideExtractAnimation=0
UseLongFileNames=1
InsideCAB=0
CABFiles=0
TargetName=TestInstaller.exe
FriendlyName=Test Installer
InstallPrompt=
DisplayName=Test Installer
PostInstallCmd=

[SourceFiles]
SourceFiles0=C:\temp_iexpress_test

[SourceFiles0]
run.bat=
'@

Set-Content -Path (Join-Path $testDir 'package.sed') -Value $sed -Encoding ASCII

$iexpress = Join-Path $env:WINDIR 'system32\iexpress.exe'
Push-Location $testDir
& $iexpress '/N' '/Q' (Join-Path $testDir 'package.sed')
$exit = $LASTEXITCODE
Pop-Location
Write-Host "EXIT=$exit"
Get-ChildItem -Path $testDir -Filter *.exe -Recurse | Select-Object FullName,Length | Format-Table -AutoSize
