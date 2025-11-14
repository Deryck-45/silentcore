Param(
    [Parameter(ValueFromRemainingArguments=$true)]
    $Args
)

# local-gradle.ps1
# Downloads a Gradle distribution into .gradle and runs it with the supplied args.
# This avoids requiring a system Gradle installation. Java 17+ is still required.

$gradleVersion = '8.5.1'
$gradleZip = "gradle-$gradleVersion-bin.zip"
$downloadUrl = "https://services.gradle.org/distributions/$gradleZip"
$installDir = Join-Path $PSScriptRoot ".gradle\gradle-$gradleVersion"

function Ensure-Java17 {
    try {
        $verOutput = & java -version 2>&1
        if ($LASTEXITCODE -ne 0) { return $false }
        # java -version prints like: java version "17.0.8" or openjdk version "17.0.8"
        if ($verOutput -match '"(\d+)') {
            $major = [int]$matches[1]
            return $major -ge 17
        }
        return $false
    } catch {
        return $false
    }
}

if (-not (Ensure-Java17)) {
    Write-Host "WARNING: Java 17+ not detected. The Gradle tooling may fail if you don't have Java 17 installed." -ForegroundColor Yellow
    Write-Host "Current java -version output:" -ForegroundColor Yellow
    & java -version 2>&1 | ForEach-Object { Write-Host $_ }
    Write-Host "If you need, install Temurin 17 via Chocolatey: `choco install temurin17 -y` (run in an elevated shell)." -ForegroundColor Yellow
}

if (-not (Test-Path $installDir)) {
    $tmp = Join-Path $env:TEMP $gradleZip
    Write-Host "Downloading Gradle $gradleVersion from $downloadUrl..."
    try {
        Invoke-WebRequest -Uri $downloadUrl -OutFile $tmp -UseBasicParsing -ErrorAction Stop
    } catch {
        Write-Error "Failed to download Gradle: $_"
        exit 1
    }

    Write-Host "Extracting to $installDir..."
    try {
        Expand-Archive -Path $tmp -DestinationPath (Join-Path $PSScriptRoot '.gradle') -Force
        Remove-Item $tmp -Force
    } catch {
        Write-Error "Failed to extract Gradle: $_"
        exit 1
    }
}

$gradleBin = Join-Path $installDir 'bin\gradle.bat'
if (-not (Test-Path $gradleBin)) {
    Write-Error "Gradle executable not found at $gradleBin"
    exit 1
}

# Build argument string
$argList = @()
if ($Args) { $argList += $Args }
& $gradleBin @argList
exit $LASTEXITCODE
