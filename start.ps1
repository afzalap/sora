# start.ps1 - starts everything Sora needs from a cold machine
# Run from the repo root: .\start.ps1

$repoRoot = $PSScriptRoot

# ---- Paths ------------------------------------------------------------------

$javaHome  = "C:\Program Files\JetBrains\IntelliJ IDEA 2026.1\jbr"
$dockerApp = "C:\Program Files\Docker\Docker\Docker Desktop.exe"
$ollamaApp = "C:\Users\apinj\AppData\Local\Programs\Ollama\ollama app.exe"

# ---- Helpers ----------------------------------------------------------------

function Write-Step($msg) { Write-Host "" ; Write-Host "> $msg" -ForegroundColor Cyan }
function Write-Ok($msg)   { Write-Host "  OK  $msg" -ForegroundColor Green }
function Write-Warn($msg) { Write-Host "  ... $msg" -ForegroundColor Yellow }

# ---- 1. Docker Desktop ------------------------------------------------------

Write-Step "Docker Desktop"

$dockerRunning = $false
try { docker info 2>$null | Out-Null; $dockerRunning = $true } catch {}

if (-not $dockerRunning) {
    Write-Warn "Docker not running - starting Docker Desktop..."
    Start-Process $dockerApp
    Write-Host "  Waiting for Docker to be ready" -NoNewline
    $timeout = 120
    $elapsed = 0
    while ($elapsed -lt $timeout) {
        Start-Sleep 3
        $elapsed += 3
        Write-Host "." -NoNewline
        $ready = $false
        try { docker info 2>$null | Out-Null; $ready = $true } catch {}
        if ($ready) { $dockerRunning = $true; break }
    }
    Write-Host ""
    if (-not $dockerRunning) {
        Write-Host "  Docker did not start in time. Open Docker Desktop manually and re-run." -ForegroundColor Red
        exit 1
    }
}
Write-Ok "Docker is running"

# ---- 2. Postgres ------------------------------------------------------------

Write-Step "Postgres"
Set-Location $repoRoot
$null = docker compose up -d
Write-Ok "sora-postgres is up (port 5432)"

# ---- 3. Ollama --------------------------------------------------------------

Write-Step "Ollama"

$ollamaRunning = Get-Process -Name "ollama app" -ErrorAction SilentlyContinue
if (-not $ollamaRunning) {
    Write-Warn "Ollama not running - starting..."
    Start-Process $ollamaApp
    Start-Sleep 3
    Write-Ok "Ollama started (http://localhost:11434)"
} else {
    Write-Ok "Ollama already running"
}

# ---- 4. Backend -------------------------------------------------------------

Write-Step "Backend (Spring Boot - http://localhost:8080)"

$backendCmd = "`$env:JAVA_HOME = '$javaHome'; Set-Location '$repoRoot\backend'; Write-Host '--- SORA BACKEND ---' -ForegroundColor Cyan; .\mvnw spring-boot:run '-Dspring-boot.run.profiles=local'"

Start-Process powershell -ArgumentList "-NoExit", "-Command", $backendCmd
Write-Ok "Backend window opened"

# ---- 5. Frontend ------------------------------------------------------------

Write-Step "Frontend (Vue - http://localhost:5173)"

$frontendCmd = "Set-Location '$repoRoot\frontend'; Write-Host '--- SORA FRONTEND ---' -ForegroundColor Cyan; npm run dev"

Start-Process powershell -ArgumentList "-NoExit", "-Command", $frontendCmd
Write-Ok "Frontend window opened"

# ---- Done -------------------------------------------------------------------

Write-Host ""
Write-Host "==========================================" -ForegroundColor White
Write-Host "  Sora is starting up" -ForegroundColor White
Write-Host "" -ForegroundColor White
Write-Host "  Frontend  ->  http://localhost:5173" -ForegroundColor White
Write-Host "  Backend   ->  http://localhost:8080" -ForegroundColor White
Write-Host "  Postgres  ->  localhost:5432 (db: sora)" -ForegroundColor White
Write-Host "  Ollama    ->  http://localhost:11434" -ForegroundColor White
Write-Host "" -ForegroundColor White
Write-Host "  Backend takes ~30s. Watch for" -ForegroundColor White
Write-Host "  'Started BackendApplication' in its window." -ForegroundColor White
Write-Host "==========================================" -ForegroundColor White
