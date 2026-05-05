# TestedCloud Local Runbook

## 1\. Purpose

This runbook explains how to run the TestedCloud on-prem lab locally using Docker Compose.

The goal is to make the local environment reproducible while avoiding the exposure of secrets or local-only credentials in Git.

This document covers:

* Local configuration files
* Docker Compose startup
* Frontend runtime configuration
* Backend environment variables
* Google Cloud credentials mounting
* Health check validation
* UI validation
* Event publishing validation
* Troubleshooting commands

## 2\. Local Architecture

The local environment runs three main services:

|Service|Purpose|
|-|-|
|`testedcloud-api`|FastAPI backend used to publish events and trace processed events|
|`testedcloud-ui`|Static frontend served by NGINX|
|`nginx`|Reverse proxy exposed locally on port `8082`|

Local access:

```text
http://localhost:8082/
```

Health endpoint:

```text
http://localhost:8082/api/health
```

Event publishing endpoint:

```text
http://localhost:8082/api/send-event
```

Trace endpoint:

```text
http://localhost:8082/api/trace-event/<event\_id>
```

## 3\. Repository Files

Important files:

|File|Purpose|Committed?|
|-|-|-|
|`docker-compose.example.yml`|Sanitized Compose example|Yes|
|`docker-compose.yml`|Local real Compose file|No|
|`frontend/config.example.js`|Sanitized frontend config example|Yes|
|`frontend/config.js`|Local real frontend config|No|
|`backend/app.py`|FastAPI backend source|Yes|
|`frontend/index.html`|Frontend source|Yes|
|`nginx/default.conf`|Reverse proxy config|Yes|
|`.gitignore`|Prevents secrets/local config from being committed|Yes|

## 4\. Files That Should Not Be Committed

The following files are local-only and should not be committed:

```text
docker-compose.yml
frontend/config.js
.env
.env.\*
\*.json service account keys
credentials/
secrets/
```

Current reason:

* `docker-compose.yml` may contain a real API key or local credential path.
* `frontend/config.js` contains the local runtime API key.
* Google credentials should remain local and should not be committed.

Validate ignored files:

```bash
git check-ignore -v frontend/config.js
```

Expected result:

```text
.gitignore:...:frontend/config.js frontend/config.js
```

## 5\. First-Time Setup

### 5.1 Clone or Enter the Repository

```bash
cd /home/dario/testedcloud-lab
```

### 5.2 Create Local Docker Compose File

Copy the sanitized example:

```bash
cp docker-compose.example.yml docker-compose.yml
```

Edit the local file:

```bash
nano docker-compose.yml
```

Update:

```text
TESTEDCLOUD\_API\_KEY=replace-me
```

to your local API key.

Also update the local Google Cloud credentials path if needed:

```text
/path/to/your/.config/gcloud:/root/.config/gcloud:ro
```

For this lab host, the local path may be:

```text
/home/dario/.config/gcloud:/root/.config/gcloud:ro
```

Do not commit this file.

### 5.3 Create Local Frontend Config

Copy the sanitized frontend config:

```bash
cp frontend/config.example.js frontend/config.js
```

Edit the local config:

```bash
nano frontend/config.js
```

Set the API key to match the backend value in `docker-compose.yml`:

```javascript
window.TESTEDCLOUD\_CONFIG = {
  API\_KEY: "your-local-api-key"
};
```

Do not commit this file.

## 6\. Google Cloud Authentication

The backend uses Google Cloud client libraries to publish messages to Pub/Sub and query BigQuery.

The Docker Compose file mounts local gcloud credentials into the container:

```text
/root/.config/gcloud/application\_default\_credentials.json
```

The backend uses this environment variable:

```text
GOOGLE\_APPLICATION\_CREDENTIALS=/root/.config/gcloud/application\_default\_credentials.json
```

Before running the lab, verify that Application Default Credentials exist on the host:

```bash
ls -la \~/.config/gcloud/application\_default\_credentials.json
```

If missing, authenticate:

```bash
gcloud auth application-default login
```

Also confirm the active project:

```bash
gcloud config get-value project
```

Expected project:

```text
majestic-layout-255620
```

If needed, set it:

```bash
gcloud config set project majestic-layout-255620
```

## 7\. Start the Local Lab

Build and start all services:

```bash
docker compose up -d --build
```

Check containers:

```bash
docker compose ps
```

Expected services:

```text
testedcloud-api
nginx
testedcloud-ui
```

Expected containers:

```text
testedcloud-api
testedcloud-nginx
testedcloud-ui
```

## 8\. Validate NGINX Reverse Proxy

Check the NGINX config:

```bash
docker exec testedcloud-nginx nginx -T | grep -n "api/health\\|send-event\\|trace-event\\|proxy\_pass"
```

Expected routes:

```text
/api/send-event
/api/trace-event/
/api/health
/
```

## 9\. Validate Health Endpoint

Run:

```bash
curl http://localhost:8082/api/health
```

Expected response:

```json
{
  "health": "healthy",
  "node": "on-prem-nuc",
  "gcp\_project": "majestic-layout-255620",
  "pubsub\_topic": "testedcloud-events"
}
```

Capture evidence when needed:

```bash
curl http://localhost:8082/api/health   > docs/evidence/api-health-validation.txt
```

## 10\. Validate UI

Open:

```text
http://localhost:8082/
```

Or use curl:

```bash
curl http://localhost:8082/
```

Expected:

* HTML page loads
* `config.js` is referenced
* Send Event button appears
* UI points to `/api/send-event`
* Trace logic points to `/api/trace-event/<event\_id>`

## 11\. Validate Frontend Runtime Config

Confirm local runtime config exists:

```bash
cat frontend/config.js
```

Expected format:

```javascript
window.TESTEDCLOUD\_CONFIG = {
  API\_KEY: "your-local-api-key"
};
```

Confirm the real key is not in committed frontend source:

```bash
grep -Rni "your-real-key" frontend/index.html frontend/config.example.js || true
```

Expected result:

```text
No output
```

## 12\. Send Test Event from CLI

You can test event publishing without the browser.

Replace `your-local-api-key` with the local API key from `docker-compose.yml`.

```bash
curl -X POST http://localhost:8082/api/send-event   -H "Content-Type: application/json"   -H "x-api-key: your-local-api-key"   -d '{
    "source": "onprem-nuc",
    "event\_type": "local\_runbook\_test",
    "message": "Event generated from local runbook validation"
  }'
```

Expected response:

```json
{
  "published": true,
  "target": "google-cloud-pubsub",
  "project\_id": "majestic-layout-255620",
  "topic": "testedcloud-events",
  "message\_id": "...",
  "event": {
    "event\_id": "...",
    "source": "onprem-nuc",
    "event\_type": "local\_runbook\_test"
  }
}
```

Copy the `event\_id` from the response.

## 13\. Trace Test Event

After waiting a few seconds, trace the event:

```bash
curl http://localhost:8082/api/trace-event/EVENT\_ID   -H "x-api-key: your-local-api-key"
```

Expected response when processed:

```json
{
  "found": true,
  "status": "processed",
  "event": {
    "event\_id": "...",
    "latency\_seconds": ...
  }
}
```

If not yet processed:

```json
{
  "found": false,
  "event\_id": "...",
  "status": "processing\_or\_not\_found"
}
```

Wait a few seconds and retry.

## 14\. Validate in BigQuery

Query recent events:

```bash
bq query --use\_legacy\_sql=false 'SELECT event\_id, received\_at, source, event\_type, origin, processed\_at, user\_email
 FROM `majestic-layout-255620.testedcloud\_events.hybrid\_events`
 ORDER BY processed\_at DESC
 LIMIT 10'
```

Expected:

* New event appears
* Source is `onprem-nuc` or `web-ui`
* Event type matches the test
* `processed\_at` is populated

## 15\. Common Commands

### Start services

```bash
docker compose up -d --build
```

### Stop services

```bash
docker compose down
```

### Restart NGINX and UI

```bash
docker compose restart nginx testedcloud-ui
```

### Restart backend

```bash
docker compose restart testedcloud-api
```

### Rebuild backend

```bash
docker compose up -d --build testedcloud-api
```

### View logs

```bash
docker compose logs -f
```

### View API logs

```bash
docker compose logs -f testedcloud-api
```

### View NGINX logs

```bash
docker compose logs -f nginx
```

## 16\. Troubleshooting

### 16.1 `/api/health` returns 404

Check NGINX route:

```bash
grep -n "api/health\\|proxy\_pass" nginx/default.conf
```

Check loaded NGINX config:

```bash
docker exec testedcloud-nginx nginx -T | grep -n "api/health\\|proxy\_pass"
```

Restart NGINX:

```bash
docker compose restart nginx
```

Expected health route:

```nginx
location /api/health {
    proxy\_pass http://testedcloud-api:8080/health;
}
```

### 16.2 UI loads but Send Event fails

Check:

```bash
cat frontend/config.js
```

Verify the API key matches `TESTEDCLOUD\_API\_KEY` in `docker-compose.yml`.

Check browser console.

Check API logs:

```bash
docker compose logs -f testedcloud-api
```

### 16.3 Backend cannot publish to Pub/Sub

Check ADC credentials:

```bash
ls -la \~/.config/gcloud/application\_default\_credentials.json
```

Check project:

```bash
gcloud config get-value project
```

Check container environment:

```bash
docker exec testedcloud-api env | grep -E "GCP\_PROJECT\_ID|PUBSUB\_TOPIC\_ID|GOOGLE\_APPLICATION\_CREDENTIALS|TESTEDCLOUD\_API\_KEY"
```

Do not paste real secrets into public docs.

### 16.4 BigQuery trace does not find event

Possible causes:

* Cloud Run consumer has not processed the message yet.
* Pub/Sub delivery delayed.
* Consumer failure.
* Event went to DLQ.
* Querying too soon.

Check Cloud Run logs:

```bash
gcloud logging read   'resource.type="cloud\_run\_revision" AND resource.labels.service\_name="testedcloud-consumer"'   --limit=20   --format="table(timestamp,severity,textPayload)"
```

Check recent BigQuery events:

```bash
bq query --use\_legacy\_sql=false 'SELECT event\_id, source, event\_type, processed\_at
 FROM `majestic-layout-255620.testedcloud\_events.hybrid\_events`
 ORDER BY processed\_at DESC
 LIMIT 10'
```

### 16.5 NGINX config changed but behavior did not change

Reload/restart NGINX:

```bash
docker compose restart nginx
```

Confirm loaded config:

```bash
docker exec testedcloud-nginx nginx -T | grep -n "api/health\\|proxy\_pass"
```

If needed, recreate:

```bash
docker compose up -d --force-recreate nginx
```

## 17\. Git Safety Checks

Before committing code, scan for secrets:

```bash
grep -RniE "darbus13|gmail.com|client\_secret|private\_key|bearer|authorization|password|secret|token"   backend frontend docker-compose.example.yml .gitignore || true
```

Check ignored files:

```bash
git check-ignore -v frontend/config.js
```

Check tracked files:

```bash
git ls-files | grep -E "frontend/config.js|docker-compose.yml" || true
```

Expected result:

```text
No output
```

This confirms that local config files are not tracked.

## 18\. Evidence to Capture

Relevant evidence files:

|Evidence File|Purpose|
|-|-|
|`docs/evidence/api-health-validation.txt`|Shows `/api/health` working|
|`docs/evidence/bigquery-events-sample.txt`|Shows processed events in BigQuery|
|`docs/evidence/cloud-run-logs-recent.txt`|Shows recent Cloud Run processing|
|`docs/evidence/pubsub-push-oidc-config.txt`|Shows Pub/Sub push/OIDC configuration|
|`docs/evidence/cloudflare-access-validation.txt`|Shows protected external access validation|

## 19\. Recommended Local Validation Sequence

Use this sequence after changes:

```text
\[ ] docker compose up -d --build
\[ ] docker compose ps
\[ ] curl http://localhost:8082/api/health
\[ ] open http://localhost:8082/
\[ ] send event from UI
\[ ] trace event from UI
\[ ] verify event in BigQuery
\[ ] check git status
\[ ] scan for secrets
```

## 20\. Portfolio Explanation

A concise way to explain this runbook:

> I documented a local runbook for TestedCloud so the lab can be reproduced safely without committing real secrets. The runbook explains how to create local config files from sanitized examples, start the Docker Compose stack, validate the API health endpoint, use the UI, send test events, trace events, and verify records in BigQuery.

## 21\. Final Positioning

This local runbook improves TestedCloud by making the lab reproducible, safer to maintain, and easier to explain.

It also demonstrates production-style discipline around local configuration, secrets hygiene, operational validation, and troubleshooting.

