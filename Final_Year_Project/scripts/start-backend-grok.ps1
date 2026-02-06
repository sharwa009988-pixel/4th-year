# Checks GROK_API_KEY in environment, frees port 8081, then starts backend
if (-not $env:GROK_API_KEY -or $env:GROK_API_KEY -eq "") {
    Write-Error "GROK_API_KEY is not set. Set it securely in your environment before running."
    exit 1
}
Write-Output "GROK_API_KEY detected in environment"

# Kill any process using port 8081
$p = (Get-NetTCPConnection -LocalPort 8081 -ErrorAction SilentlyContinue).OwningProcess
if ($p) {
    foreach ($pid in $p) {
        Write-Output "Killing PID $pid"
        taskkill /PID $pid /F
    }
} else {
    Write-Output "No process found on port 8081"
}

# Start backend
Set-Location -Path "backend"
mvn org.springframework.boot:spring-boot-maven-plugin:3.2.0:run
