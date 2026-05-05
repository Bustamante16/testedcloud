# TestedCloud — Hybrid Cloud Architecture Lab

TestedCloud is a hybrid cloud portfolio lab designed to demonstrate how on-prem infrastructure can securely integrate with Google Cloud services for event ingestion, serverless processing, analytics, observability, IAM hardening, private networking, and cost-conscious operations.

The project connects an on-prem Ubuntu/Docker environment running on an Intel NUC with Google Cloud services including Pub/Sub, Cloud Run, BigQuery, Looker Studio, custom VPC networking, private VM access through IAP, Cloudflare Access for protected external access, and Cloud Monitoring alert policies for key operational failure modes.

## 1\. Purpose

TestedCloud was built to translate industrial networking, secure infrastructure, and on-prem systems experience into practical Google Cloud architecture patterns relevant to roles such as:

* Google Cloud Customer Engineer
* Cloud Architect
* Solutions Architect
* Hybrid Cloud Engineer
* Infrastructure / Platform Engineer

The lab demonstrates not only a working cloud pipeline, but also the supporting architecture documentation, validation evidence, troubleshooting history, IAM hardening, monitoring alerts, cost governance, and operational runbooks.

## 2\. Portfolio Narrative

The main narrative of TestedCloud is:

> I built TestedCloud to bridge my background in industrial infrastructure, secure networking, and on-prem systems with modern Google Cloud architecture. The platform demonstrates how on-prem workloads can integrate with cloud-native services for event ingestion, serverless processing, analytics, observability, IAM hardening, private networking, operational troubleshooting, and cost-aware operations.

Final positioning:

> TestedCloud bridges industrial infrastructure and modern cloud-native application development on Google Cloud.

## 3\. Current Architecture

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
Cloud Run: testedcloud-consumer
    |
    v
BigQuery: testedcloud\_events.hybrid\_events
    |
    v
BigQuery Views / Looker Studio Dashboard
```

Protected external UI:

```text
https://ui.testedcloud.com
```

Local lab access:

```text
http://localhost:8082/
```

Health endpoint:

```text
http://localhost:8082/api/health
```

## 4\. Core Components

### On-Prem Lab

|Component|Value|
|-|-|
|Hostname|`ubuserver`|
|Hardware|`Intel NUC7i3BNH`|
|OS|`Ubuntu 24.04.4 LTS`|
|Runtime|Docker Compose|
|Local project path|`/home/dario/testedcloud-lab`|
|Local port|`8082`|
|Protected domain|`ui.testedcloud.com`|

Docker services:

|Service|Purpose|
|-|-|
|`testedcloud-api`|FastAPI backend for event publishing and tracing|
|`testedcloud-ui`|Static frontend UI|
|`nginx`|Reverse proxy exposed locally on port `8082`|

### Google Cloud

|Area|Resource|
|-|-|
|Project ID|`majestic-layout-255620`|
|Project number|`644725546932`|
|Region|`us-central1`|
|Pub/Sub topic|`testedcloud-events`|
|DLQ topic|`testedcloud-events-dlq`|
|Push subscription|`testedcloud-consumer-sub`|
|DLQ subscription|`testedcloud-events-dlq-sub`|
|Cloud Run service|`testedcloud-consumer`|
|BigQuery dataset|`testedcloud\_events`|
|BigQuery table|`hybrid\_events`|
|Monitoring alerts|Cloud Run 5xx, Pub/Sub backlog, DLQ messages|

### Networking

|Resource|Value|
|-|-|
|VPC|`testedcloud-vpc`|
|App subnet|`testedcloud-subnet-app` — `10.10.1.0/24`|
|Data subnet|`testedcloud-subnet-data` — `10.10.2.0/24`|
|Private VM|`testedcloud-vm-app-test`|
|Private VM IP|`10.10.1.2`|
|VM access|IAP SSH|
|IAP source range|`35.235.240.0/20`|

## 5\. Key Design Areas

TestedCloud demonstrates the following architecture areas:

* Hybrid on-prem to cloud integration
* Event-driven architecture with Pub/Sub
* Serverless event processing with Cloud Run
* BigQuery analytics and dashboard views
* Looker Studio dashboarding
* IAM least-privilege hardening
* Dedicated service accounts by workload responsibility
* Pub/Sub OIDC push authentication to Cloud Run
* Dead-letter queue failure handling
* Cloud Monitoring alerts for operational failure modes
* Private VPC design
* Private VM access through IAP
* Cloudflare Access protection for external UI
* Cost-conscious architecture
* Budget alert planning
* Evidence-based documentation
* Local reproducibility through runbooks

## 6\. Security Highlights

Implemented security improvements:

* Cloud Run migrated from the default Compute Engine service account to a dedicated service account.
* Broad roles were removed from the default Compute Engine service account.
* Pub/Sub invokes Cloud Run using OIDC authentication.
* Private VM has no external IP.
* SSH access is restricted through IAP.
* Cloudflare Access protects `ui.testedcloud.com`.
* Direct public router port forwarding to the lab was removed.
* Frontend API key was moved out of committed frontend source.
* Local sensitive config files are excluded through `.gitignore`.
* A sanitized `docker-compose.example.yml` is provided instead of committing the real local Compose file.
* Monitoring policy definitions are versioned with notification channel IDs redacted.

## 7\. Validated Outcomes

The following outcomes have been validated and documented:

|Validation|Status|
|-|-|
|Web UI event to Pub/Sub|Validated|
|Pub/Sub push to Cloud Run|Validated|
|Cloud Run insert to BigQuery|Validated|
|BigQuery dashboard views|Validated|
|BigQuery latency metrics|Validated|
|Cloud Run dedicated runtime service account|Validated|
|Pub/Sub OIDC invoker configuration|Validated|
|DLQ policy with max delivery attempts|Validated|
|Malformed message sent to DLQ|Validated|
|Private VM in custom VPC|Validated|
|IAP SSH firewall rule|Validated|
|Cloudflare Access protection|Validated|
|Public port forwarding bypass removed|Validated|
|`/api/health` endpoint routing|Validated|
|Cloud Monitoring alert policies|Configured|
|Billing project link sanitized|Validated|
|Budget alert strategy|Documented|

Configured Cloud Monitoring alerts:

|Alert Policy|Status|
|-|-|
|`TestedCloud - Cloud Run 5xx errors`|Enabled|
|`TestedCloud - Pub/Sub consumer backlog`|Enabled|
|`TestedCloud - DLQ messages detected`|Enabled|

## 8\. Observed Metrics

Current observed analytics metrics include:

|Metric|Value|
|-|-|
|Total events|72+|
|Web UI events|63|
|On-prem NUC events|3|
|Private GCP VM events|2|
|p50 latency|424 ms|
|p95 latency|5321 ms|
|p99 latency|5890 ms|

## 9\. Documentation Index

### Core Documentation

|Document|Description|
|-|-|
|[`docs/core-platform.md`](docs/core-platform.md)|Main technical overview of the TestedCloud Core Platform|
|[`docs/architecture/hybrid-architecture.md`](docs/architecture/hybrid-architecture.md)|Hybrid architecture diagrams and design explanation|
|[`docs/evidence/validated-tests.md`](docs/evidence/validated-tests.md)|Executive index of validated tests and evidence|

### Security

|Document|Description|
|-|-|
|[`docs/security/iam-hardening.md`](docs/security/iam-hardening.md)|IAM hardening model and validation|
|[`docs/security/cloudflare-access.md`](docs/security/cloudflare-access.md)|Cloudflare Access security model and bypass remediation|

### Operations

|Document|Description|
|-|-|
|[`docs/operations/local-runbook.md`](docs/operations/local-runbook.md)|How to run the local lab safely and reproducibly|
|[`docs/operations/dlq-validation.md`](docs/operations/dlq-validation.md)|Pub/Sub DLQ failure-path validation|
|[`docs/operations/troubleshooting-log.md`](docs/operations/troubleshooting-log.md)|Troubleshooting history and lessons learned|
|[`docs/operations/monitoring-alerts.md`](docs/operations/monitoring-alerts.md)|Cloud Monitoring alert strategy and operational response model|

### Cost and Governance

|Document|Description|
|-|-|
|[`docs/cost/cost-considerations.md`](docs/cost/cost-considerations.md)|Cost-conscious architecture analysis|
|[`docs/cost/budget-alerts.md`](docs/cost/budget-alerts.md)|Budget alert strategy|

### Evidence

|Document / Folder|Description|
|-|-|
|[`docs/evidence/README.md`](docs/evidence/README.md)|Evidence collection strategy and command index|
|`docs/evidence/\*.txt`|Sanitized command outputs and validation evidence|

### Monitoring Policy Definitions

|File|Description|
|-|-|
|[`monitoring/policies/cloud-run-5xx-alert.json`](monitoring/policies/cloud-run-5xx-alert.json)|Sanitized Cloud Run 5xx alert policy definition|
|[`monitoring/policies/pubsub-backlog-alert.json`](monitoring/policies/pubsub-backlog-alert.json)|Sanitized Pub/Sub backlog alert policy definition|
|[`monitoring/policies/dlq-message-alert.json`](monitoring/policies/dlq-message-alert.json)|Sanitized DLQ message alert policy definition|

## 10\. Evidence Examples

The `docs/evidence` folder includes sanitized evidence for:

* Cloud Run runtime service account
* Pub/Sub push OIDC configuration
* Pub/Sub DLQ configuration
* IAM cleanup after hardening
* BigQuery event samples
* BigQuery latency metrics
* Private VM configuration
* IAP firewall rule
* Billing project link
* Budget alert plan
* Cloudflare Access validation
* API health endpoint validation
* Monitoring alert plan
* Configured Cloud Monitoring alert policies

Sensitive information such as API keys, billing account IDs, Cloudflare tokens, notification channel IDs, private keys, and real local runtime configuration is intentionally excluded or sanitized.

## 11\. Local Development

This repository includes a sanitized Docker Compose example:

```text
docker-compose.example.yml
```

The real local file is intentionally not committed:

```text
docker-compose.yml
```

To run locally, follow:

```text
docs/operations/local-runbook.md
```

High-level local setup:

```bash
cp docker-compose.example.yml docker-compose.yml
cp frontend/config.example.js frontend/config.js
```

Then update local-only values:

```text
TESTEDCLOUD\_API\_KEY
Google Cloud ADC credential path
frontend/config.js API\_KEY
```

Start the lab:

```bash
docker compose up -d --build
```

Validate:

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

## 12\. Git and Secret Hygiene

The repository intentionally avoids committing:

* `docker-compose.yml`
* `frontend/config.js`
* `.env` files
* service account key files
* API keys
* Cloudflare secrets
* OAuth tokens
* notification channel IDs
* private keys

Before committing changes, run:

```bash
grep -RniE "password|secret|token|bearer|authorization|client\_secret|private\_key|api\_key"   backend frontend docs docker-compose.example.yml .gitignore || true
```

Check that local-only files are not tracked:

```bash
git ls-files | grep -E "frontend/config.js|docker-compose.yml" || true
```

Expected result:

```text
No output
```

## 13\. Current Limitations

Known limitations and future improvements:

|Item|Status|
|-|-|
|Cloud Monitoring alerts|Configured for Cloud Run 5xx, Pub/Sub backlog, and DLQ messages|
|Budget alert actual console status|Planned / documented|
|Secret management|Needs improvement|
|API key model|Improved but still simple|
|Public portfolio landing page|Not complete|
|Dashboard final polish|In progress|
|Industrial telemetry module|Planned|
|BGP hybrid connectivity lab|Planned|
|Vertex AI analytics|Planned|

## 14\. Roadmap

### Phase 1 — Core Platform Completion

* Finalize documentation
* Create architecture diagrams
* Add evidence artifacts
* Add `/api/health`
* Add local runbook
* Improve dashboard
* Add monitoring alerts
* Add budget alerts
* Prepare public landing page

### Phase 2 — Public Portfolio Site

Create a public portfolio landing page at:

```text
https://testedcloud.com
```

Protected operational lab UI remains:

```text
https://ui.testedcloud.com
```

### Phase 3 — TestedChat Release 1

Build an Android/Firebase messaging application to demonstrate:

* Firebase Authentication
* Firestore real-time messaging
* Basic user profiles
* Mobile-first application architecture
* Privacy/data safety foundations

### Phase 4 — TestedChat Release 2

Extend TestedChat with:

* Cloud Run APIs
* Pub/Sub events
* BigQuery analytics
* Looker Studio dashboards
* IAM/service account hardening
* Observability and auditability

### Phase 5 — Industrial Telemetry

Add industrial telemetry modules using:

* SINEC NMS REST API
* Ruggedcom / SCALANCE telemetry
* SNMP
* Syslog
* Device inventory
* Network event analytics

### Phase 6 — Hybrid Connectivity and AI

Future roadmap:

* BGP / hybrid connectivity lab
* Advanced analytics
* BigQuery ML or Vertex AI experiments
* Edge-to-cloud operational insights

## 15\. Interview Explanation

A concise explanation:

> TestedCloud is a hybrid cloud portfolio lab I built to demonstrate how on-prem workloads can securely integrate with Google Cloud. The on-prem side runs on an Ubuntu-based Intel NUC using Docker Compose. Events from the local UI/API are published to Pub/Sub, processed by Cloud Run, stored in BigQuery, and visualized through dashboard views. I also implemented Cloudflare Access, IAM hardening, Pub/Sub OIDC authentication, DLQ validation, private VPC networking, IAP SSH access, Cloud Monitoring alerts, cost documentation, and evidence-based validation.

A more technical version:

> The platform demonstrates event-driven hybrid integration using Pub/Sub and Cloud Run, with BigQuery as the analytics layer. I separated service identities using dedicated service accounts, removed broad permissions from the default Compute Engine service account, configured Pub/Sub push with OIDC authentication, validated failure handling through a DLQ, and configured Cloud Monitoring alerts for Cloud Run 5xx errors, Pub/Sub backlog, and DLQ messages. I also documented evidence for Cloud Run identity, Pub/Sub config, BigQuery records, private networking, IAP firewall rules, Cloudflare Access protection, and monitoring policy configuration.

## 16\. Final Positioning

TestedCloud demonstrates practical Google Cloud architecture across:

* Hybrid cloud integration
* Serverless event processing
* IAM hardening
* Private networking
* Secure access
* Analytics
* Observability
* Cost awareness
* Troubleshooting
* Portfolio-grade documentation

It is designed to show the ability to build, secure, validate, monitor, document, and explain a realistic cloud architecture from end to end.

