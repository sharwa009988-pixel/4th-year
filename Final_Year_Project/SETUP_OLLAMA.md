
# Grok (xAI) Setup Guide

This project now uses Grok (xAI) instead of a local Ollama server. Below are the steps to configure the application to call the hosted Grok API.

## Configure API Key

Provide your Grok API key via an environment variable. Do NOT commit the key to source control.

Windows (PowerShell persistent):
```powershell
setx GROK_API_KEY "<your_api_key_here>"
```

Temporary for current session:
```powershell
$env:GROK_API_KEY = "<your_api_key_here>"
```

## Optional: override base URL or endpoint
If you use a proxy or custom endpoint, set `GROK_BASE_URL` and `GROK_ENDPOINT`. Defaults are `https://api.grok.x.ai` and `/v1/generate` respectively.

## Configure Backend
Update `src/main/resources/application.yml` only if you want to change defaults. The app reads:

```yaml
spring:
  ai:
    grok:
      base-url: https://api.grok.x.ai
      endpoint: /v1/generate
      api-key: ${GROK_API_KEY}
      model: grokxai-RUux... (configured via env)
      chat:
        temperature: 0.7
```

## Start Backend

```bash
cd backend
mvn spring-boot:run
```

The backend will call Grok for generating questions and evaluations. Ensure `GROK_API_KEY` is available in the environment where the backend runs.

## Troubleshooting

- If you receive 401/403, ensure `GROK_API_KEY` is correct and present in the process environment.
- If responses are empty, check `spring.ai.grok.endpoint` and `spring.ai.grok.base-url` if you use a proxy.
- Consult backend logs for full response shapes; the service tries to parse several common response formats.

## Next Steps

1. Start backend: `mvn spring-boot:run`
2. Start frontend: `npm run dev`
3. Test interview flows
