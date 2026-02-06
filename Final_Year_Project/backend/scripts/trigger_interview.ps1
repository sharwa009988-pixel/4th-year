$body='{"email":"copilot_test_20260204_2248@example.com","name":"Copilot Test","password":"Test1234"}'
Write-Output "Registering user..."
try {
  $resp=Invoke-RestMethod -Uri 'http://localhost:8081/api/auth/register' -Method POST -ContentType 'application/json' -Body $body -ErrorAction Stop
  Write-Output "Registration token: $($resp.token)"
} catch {
  Write-Output "Register failed, attempting login..."
  $loginBody='{"email":"copilot_test_20260204_2248@example.com","password":"Test1234"}'
  $resp=Invoke-RestMethod -Uri 'http://localhost:8081/api/auth/login' -Method POST -ContentType 'application/json' -Body $loginBody -ErrorAction Stop
  Write-Output "Login token: $($resp.token)"
}
$token = $resp.token
$req='{"type":"TECHNICAL","topic":"algorithms","difficulty":"MEDIUM","numberOfQuestions":1}'
Write-Output "Starting interview..."
$start = Invoke-RestMethod -Uri 'http://localhost:8081/api/interviews/start' -Method POST -ContentType 'application/json' -Headers @{ Authorization = "Bearer $token" } -Body $req -ErrorAction Stop
Write-Output "Start response:"
$start | ConvertTo-Json -Depth 5
