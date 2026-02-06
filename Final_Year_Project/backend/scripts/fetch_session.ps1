$login='{"email":"copilot_test_20260204_2248@example.com","password":"Test1234"}'
$resp=Invoke-RestMethod -Uri 'http://localhost:8081/api/auth/login' -Method POST -ContentType 'application/json' -Body $login
$token=$resp.token
Write-Output "Fetching session 5..."
Invoke-RestMethod -Uri 'http://localhost:8081/api/interviews/sessions/5' -Method GET -ContentType 'application/json' -Headers @{ Authorization = "Bearer $token" } | ConvertTo-Json -Depth 10
