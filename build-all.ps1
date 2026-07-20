$ErrorActionPreference = "Stop"

$versions = @("1.21.11")

foreach ($version in $versions) {
    Write-Host "WayMarker mod to reach your destination easily $version"
    & .\gradlew.bat clean build "-PtargetVersion=$version"
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
}
