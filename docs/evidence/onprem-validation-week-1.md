# On-Prem Validation — Week 1

Date: May 11, 2026
Scope: Docker, Docker Compose, NGINX reverse proxy, FastAPI backend, and Pub/Sub publishing from on-prem

## Objective

Validate that the on-premises TestedCloud environment is operational before continuing with GCP pipeline validation.

## Environment

Repository path:

    ~/testedcloud-lab

Correct local TestedCloud route:

    http://localhost:8082

Incorrect routes identified during validation:

    http://localhost:8080
    http://localhost

Reason:

- localhost:8080 points to the WordPress container.
- localhost:80 points to the general nginx-proxy / WordPress route.
- localhost:8082 points to the TestedCloud NGINX reverse proxy.

## Docker Validation

Docker engine was available and running.

Main TestedCloud containers:

- testedcloud-api: Up
- testedcloud-nginx: Up
- testedcloud-ui: Up

Supporting containers also running on the NUC included WordPress, MariaDB, Cloudflare Tunnel, Portainer, and a general NGINX proxy.

## Docker Compose Services

The TestedCloud Docker Compose stack includes:

- testedcloud-api
- testedcloud-nginx
- testedcloud-ui

The testedcloud-nginx service exposes:

    8082:80

Therefore, local TestedCloud validation should use:

    http://localhost:8082

## NGINX Root Validation

Command:

    curl -i http://localhost:8082/

Result:

    HTTP/1.1 200 OK
    Server: nginx/1.29.5

The UI loaded successfully and displayed:

    TestedCloud Hybrid Lab
    On-prem NUC → Pub/Sub → Cloud Run → BigQuery

Status:

    PASS

## FastAPI Health Validation Through NGINX

Command:

    curl -i http://localhost:8082/api/health

Result:

    HTTP/1.1 200 OK
    Server: nginx/1.29.5
    Content-Type: application/json

Response:

    {
      "health": "healthy",
      "node": "on-prem-nuc",
      "gcp_project": "majestic-layout-255620",
      "pubsub_topic": "testedcloud-events"
    }

Status:

    PASS

## Event Publishing Validation Through NGINX and FastAPI

Command:

    curl -i -X POST http://localhost:8082/api/send-event \
      -H "Content-Type: application/json" \
      -H "x-api-key: <redacted>" \
      -d '{
        "source": "testedcloud-core",
        "event_type": "week_1_onprem_validation",
        "message": "Testing on-prem NGINX to FastAPI route",
        "environment": "onprem"
      }'

Result:

    HTTP/1.1 200 OK
    Server: nginx/1.29.5
    Content-Type: application/json

Response summary:

    {
      "published": true,
      "target": "google-cloud-pubsub",
      "project_id": "majestic-layout-255620",
      "topic": "testedcloud-events",
      "message_id": "19022738486043056",
      "event": {
        "event_id": "d7ee44d5-8221-4fa0-a1ca-17179c14cb11",
        "source": "testedcloud-core",
        "event_type": "week_1_onprem_validation",
        "origin": "on-prem-nuc"
      }
    }

Status:

    PASS

## Findings

### Finding 1 — Port 8080 is not TestedCloud API

localhost:8080 points to the WordPress container. Testing /health on this port results in a WordPress redirect.

### Finding 2 — Port 80 is not TestedCloud NGINX

localhost:80 points to the general nginx-proxy and WordPress route. Testing /api/health on this port returns a WordPress page-not-found response.

### Finding 3 — Correct local route is port 8082

The correct local TestedCloud validation route is:

    http://localhost:8082

## Final Result

On-prem validation result:

    PASS

The TestedCloud on-prem layer is operational through Docker Compose, NGINX reverse proxy, FastAPI backend, and Pub/Sub publishing.
