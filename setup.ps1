$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$Backend = Join-Path $Root "backend"
$Web = Join-Path $Root "web-admin"

function Invoke-Checked {
    param([string]$Command, [string[]]$Arguments)
    & $Command @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Command failed: $Command $($Arguments -join ' ')"
    }
}

$PythonCommand = $null
$PythonPrefix = @()
if (Get-Command py -ErrorAction SilentlyContinue) {
    $PythonCommand = "py"
    $PythonPrefix = @("-3")
} elseif (Get-Command python -ErrorAction SilentlyContinue) {
    $PythonCommand = "python"
} else {
    throw "Python 3 is required. Install Python 3.10 or newer."
}

if (-not (Get-Command node -ErrorAction SilentlyContinue) -or -not (Get-Command npm -ErrorAction SilentlyContinue)) {
    throw "Node.js and npm are required. Install Node.js 20 or newer."
}

$PythonVersion = & $PythonCommand @PythonPrefix --version
if ($PythonVersion -notmatch "Python (\d+)\.(\d+)") {
    throw "Unable to detect the Python version."
}
if ([int]$Matches[1] -lt 3 -or ([int]$Matches[1] -eq 3 -and [int]$Matches[2] -lt 10)) {
    throw "Python 3.10 or newer is required."
}
$NodeVersion = & node --version
if ($NodeVersion -notmatch "v?(\d+)") {
    throw "Unable to detect the Node.js version."
}
if ([int]$Matches[1] -lt 20) {
    throw "Node.js 20 or newer is required."
}
Write-Host "Using $PythonVersion"
Write-Host "Using Node.js $NodeVersion"

$BackendEnv = Join-Path $Backend ".env"
$WebEnv = Join-Path $Web ".env"

if (-not (Test-Path $BackendEnv)) {
    Copy-Item (Join-Path $Backend ".env.example") $BackendEnv
}
if (-not (Test-Path $WebEnv)) {
    Copy-Item (Join-Path $Web ".env.example") $WebEnv
}

$VenvPython = Join-Path $Backend ".venv\Scripts\python.exe"
if (-not (Test-Path $VenvPython)) {
    Invoke-Checked $PythonCommand ($PythonPrefix + @("-m", "venv", (Join-Path $Backend ".venv")))
}
Invoke-Checked $VenvPython @("-m", "pip", "install", "--upgrade", "pip")
Invoke-Checked $VenvPython @("-m", "pip", "install", "-r", (Join-Path $Backend "requirements.txt"))

Push-Location $Web
try {
    Invoke-Checked "npm" @("install")
} finally {
    Pop-Location
}

Write-Host ""
Write-Host "Setup completed."
Write-Host "Configure backend/.env and web-admin/.env before starting the applications."
Write-Host "Create the PostgreSQL database and run backend/schemas/schema.sql."
Write-Host "Start the API: cd backend; .venv\Scripts\python.exe -m uvicorn main:app --reload --host 0.0.0.0 --port 8000"
Write-Host "Start Web Admin: cd web-admin; npm run dev"
