# TestedCloud Monitoring Alerts

## 1\. Purpose

This document describes the monitoring alert strategy for the TestedCloud Core Platform.

The goal is to improve operational visibility by detecting failures, backlog growth, dead-letter queue activity, health endpoint issues, and cost-related signals.

This monitoring plan supports the TestedCloud portfolio by showing that the lab is not only functional, but also designed with operational awareness.

## 2\. Monitoring Scope

The initial monitoring scope focuses on:

* Cloud Run consumer errors
* Pub/Sub subscription backlog
* Pub/Sub dead-letter queue activity
* API health endpoint availability
* Budget and cost awareness
* Future dashboard and latency monitoring

## 3\. Current Architecture Context

Current event flow:

```text
On-prem UI/API
    |
    v
Pub/Sub Topic: testedcloud-events
    |
    v
Push Subscription: testedcloud-consumer-sub
    |
    v
Cloud Run Consumer: testedcloud-consumer
    |
    v
BigQuery: testedcloud\_events.hybrid\_events
```

Failure path:

```text
Failed Pub/Sub delivery
    |
    v
Retry policy
    |
    v
Max delivery attempts: 5
    |
    v
Dead-letter topic: testedcloud-events-dlq
    |
    v
DLQ subscription: testedcloud-events-dlq-sub
```

## 4\. Recommended Alerts

|Alert|Purpose|Priority|
|-|-|-|
|Cloud Run 5xx errors|Detect processing failures|High|
|Pub/Sub subscription backlog|Detect stuck or delayed message processing|High|
|DLQ message count greater than 0|Detect failed messages routed to DLQ|High|
|`/api/health` uptime check|Detect local API/reverse proxy health issues|Medium|
|Cloud Run latency increase|Detect slow processing|Medium|
|Budget threshold alerts|Detect unexpected spend|High|

## 5\. Alert 1 — Cloud Run 5xx Errors

### Purpose

Detect when the Cloud Run consumer returns server-side errors.

Service:

```text
testedcloud-consumer
```

Reason:

Cloud Run 5xx errors may indicate:

* Bad payload handling issues
* Application exceptions
* BigQuery insert failures
* Pub/Sub push delivery failures
* Runtime or dependency issues

### Recommended Condition

```text
Cloud Run 5xx response count > 0 over 5 minutes
```

### Suggested Severity

```text
High
```

### Operational Response

If triggered:

1. Check Cloud Run logs.
2. Identify recent deployment or code change.
3. Check Pub/Sub retry behavior.
4. Check DLQ messages.
5. Confirm BigQuery inserts.
6. Fix payload or consumer logic if needed.

## 6\. Alert 2 — Pub/Sub Subscription Backlog

### Purpose

Detect when messages are accumulating in the main subscription.

Subscription:

```text
testedcloud-consumer-sub
```

Reason:

A growing backlog may indicate:

* Cloud Run is failing
* Cloud Run is unavailable
* Pub/Sub push delivery is failing
* OIDC invocation is misconfigured
* Consumer processing is too slow

### Recommended Condition

```text
Undelivered messages > 0 for more than 5 minutes
```

For a small lab, even a small backlog is worth investigating.

### Suggested Severity

```text
High
```

### Operational Response

If triggered:

1. Check Pub/Sub subscription status.
2. Check Cloud Run service health.
3. Check Cloud Run logs.
4. Check IAM/OIDC configuration.
5. Confirm the push endpoint is correct.
6. Check DLQ.

## 7\. Alert 3 — DLQ Message Count Greater Than Zero

### Purpose

Detect when failed messages are routed to the dead-letter queue.

DLQ subscription:

```text
testedcloud-events-dlq-sub
```

Reason:

Any message in the DLQ means at least one message failed normal processing after retry exhaustion.

### Recommended Condition

```text
DLQ undelivered messages > 0
```

### Suggested Severity

```text
High
```

### Operational Response

If triggered:

1. Pull messages from DLQ.
2. Inspect payload.
3. Identify validation or processing failure.
4. Check Cloud Run logs.
5. Determine whether to fix producer, consumer, or schema.
6. Optionally republish a corrected message.

## 8\. Alert 4 — API Health Endpoint Uptime

### Purpose

Validate the local reverse proxy and API health endpoint.

Local endpoint:

```text
http://localhost:8082/api/health
```

External protected endpoint, if reachable through Cloudflare Access:

```text
https://ui.testedcloud.com/api/health
```

Current validated local response:

```json
{
  "health": "healthy",
  "node": "on-prem-nuc",
  "gcp\_project": "majestic-layout-255620",
  "pubsub\_topic": "testedcloud-events"
}
```

### Recommended Condition

```text
Health endpoint is unavailable or does not return HTTP 200
```

### Suggested Severity

```text
Medium
```

### Operational Response

If triggered:

1. Check Docker containers.
2. Check NGINX config.
3. Check backend API container.
4. Confirm `/api/health` route.
5. Restart NGINX or backend if needed.

## 9\. Alert 5 — Cloud Run Latency Increase

### Purpose

Detect when event processing becomes slower than expected.

Reason:

Latency increase may indicate:

* Cloud Run cold starts
* BigQuery insert delays
* Application processing issues
* Pub/Sub delivery delay
* Downstream dependency latency

### Recommended Condition

```text
Cloud Run request latency above normal baseline for more than 5 minutes
```

### Suggested Severity

```text
Medium
```

### Operational Response

If triggered:

1. Check Cloud Run request latency.
2. Check Cloud Run logs.
3. Check BigQuery inserts.
4. Compare with BigQuery latency metrics.
5. Review recent changes.

## 10\. Alert 6 — Budget Alerts

Budget alert strategy is documented separately:

```text
docs/cost/budget-alerts.md
```

Evidence:

```text
docs/evidence/budget-alerts-config.txt
```

Recommended thresholds:

|Threshold|Purpose|
|-|-|
|50%|Early awareness|
|80%|Action threshold|
|100%|Stop and review|

## 11\. Suggested Evidence Files

Recommended evidence files:

```text
docs/evidence/monitoring-alerts-plan.txt
docs/evidence/cloud-run-5xx-alert-config.txt
docs/evidence/pubsub-backlog-alert-config.txt
docs/evidence/dlq-alert-config.txt
docs/evidence/api-health-uptime-check.txt
```

If alert policies are created manually through the Google Cloud Console, capture sanitized summaries instead of screenshots that expose sensitive information.

## 12\. Suggested Manual Evidence File

Create a sanitized monitoring evidence file:

```bash
cat > docs/evidence/monitoring-alerts-plan.txt <<'EOF'
TestedCloud Monitoring Alerts Plan

Project: majestic-layout-255620

Planned / recommended alert policies:
1. Cloud Run 5xx errors for testedcloud-consumer
2. Pub/Sub backlog for testedcloud-consumer-sub
3. DLQ message count for testedcloud-events-dlq-sub
4. API health endpoint uptime check for /api/health
5. Budget alerts at 50%, 80%, 100%

Current status:
- /api/health endpoint implemented and validated
- Budget alert strategy documented
- Alert policies planned / to be configured

Sanitization:
This file does not include notification channel IDs, emails, tokens, or private credentials.
EOF
```

## 13\. Suggested Validation Commands

### Check Cloud Run logs

```bash
gcloud logging read \\
  'resource.type="cloud\_run\_revision" AND resource.labels.service\_name="testedcloud-consumer"' \\
  --limit=20 \\
  --format="table(timestamp,severity,textPayload)"
```

### Check Pub/Sub main subscription

```bash
gcloud pubsub subscriptions describe testedcloud-consumer-sub
```

### Check DLQ subscription

```bash
gcloud pubsub subscriptions describe testedcloud-events-dlq-sub
```

### Pull DLQ messages

```bash
gcloud pubsub subscriptions pull testedcloud-events-dlq-sub \\
  --limit=10 \\
  --auto-ack
```

### Validate local health endpoint

```bash
curl http://localhost:8082/api/health
```

## 14\. Recommended Next Implementation Order

Recommended order:

1. Create monitoring alerts documentation.
2. Create sanitized monitoring evidence file.
3. Configure budget alerts in Console.
4. Configure Cloud Run 5xx alert.
5. Configure Pub/Sub backlog alert.
6. Configure DLQ message count alert.
7. Optionally configure uptime check.
8. Capture sanitized evidence.
9. Update `validated-tests.md`.

## 15\. Monitoring-to-Architecture Mapping

|Architecture Component|Monitoring Signal|
|-|-|
|Cloud Run consumer|5xx errors, latency, request count|
|Pub/Sub main subscription|Backlog / undelivered messages|
|Pub/Sub DLQ subscription|DLQ message count|
|On-prem API / NGINX|`/api/health` uptime check|
|BigQuery analytics layer|Latency metrics and query outputs|
|Cost governance|Budget alerts|

## 16\. Current Monitoring Status

|Monitoring Item|Status|
|-|-|
|`/api/health` endpoint|Implemented and validated|
|Budget alert strategy|Documented|
|Budget evidence file|Created as sanitized planned evidence|
|Cloud Run 5xx alert|Planned|
|Pub/Sub backlog alert|Planned|
|DLQ message count alert|Planned|
|API uptime check|Planned|
|Monitoring alert evidence|To be created|

## 17\. Interview Explanation

A concise way to explain this monitoring plan:

> I added a monitoring plan for TestedCloud focused on the most important operational failure modes: Cloud Run 5xx errors, Pub/Sub backlog growth, DLQ messages, and API health availability. This complements the existing evidence-based documentation by showing how I would detect and respond to production-style issues in an event-driven hybrid cloud architecture.

A more technical version:

> The monitoring strategy maps directly to the event pipeline. Cloud Run 5xx alerts detect consumer failures, Pub/Sub backlog alerts detect delivery or processing delays, DLQ alerts detect retry exhaustion, and the `/api/health` check validates the local API/reverse proxy path. This gives the lab an operational layer beyond basic deployment.

## 18\. Customer Engineer Relevance

This monitoring plan is relevant to Customer Engineer and Cloud Architect roles because it demonstrates the ability to:

* Identify operational failure modes
* Translate architecture into alerting strategy
* Prioritize actionable alerts
* Avoid relying only on manual testing
* Connect observability to reliability
* Explain monitoring in customer-facing terms
* Design a more production-ready cloud architecture

## 19\. Final Positioning

The monitoring alert strategy strengthens TestedCloud by adding an operational observability layer.

It shows that the platform was designed not only to work, but to be monitored, validated, troubleshot, and improved.

