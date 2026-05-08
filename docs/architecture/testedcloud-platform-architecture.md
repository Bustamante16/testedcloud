# TesteCloud Platform Architecture

**Document version:** v1.0  
**Purpose:** Portfolio architecture document  
**Primary brand:** TesteCloud  
**Main concept:** Hybrid cloud portfolio platform built on a physical home rack, on-prem Linux/Docker workloads, secure connectivity, Google Cloud event pipelines, analytics, observability, and AI-enabled operational use cases.

\---

## 1\. Executive Summary

**TesteCloud** is a hybrid cloud portfolio platform designed to demonstrate practical cloud architecture, edge-to-cloud integration, infrastructure validation, security, observability, and AI-assisted operations.

The platform is built around a physical on-prem rack named **TesteCloud Rack**, which acts as a small infrastructure lab or mini data center. The rack hosts local networking equipment, Linux/Docker workloads, APIs, test runners, and telemetry collectors. These local services integrate securely with Google Cloud through event-driven patterns using Pub/Sub, Cloud Run, BigQuery, and Looker Studio.

TesteCloud is not intended to be a single application. It is a modular platform that supports several portfolio projects, including:

* **TesteCloud Core Platform** — hybrid connectivity, event ingestion, cloud processing, analytics, and security baseline.
* **TesteCloud Rack** — physical infrastructure validation, network testing, device inventory, and test automation.
* **TesteCloud Chat** — Android messaging application using Firebase and Google Cloud services.
* **TesteCloud Monitor** — observability, health checks, dashboards, and incident tracking.
* **TesteCloud AI Ops Assistant** — AI-assisted troubleshooting, log analysis, runbook retrieval, and incident explanation.
* **TesteCloud Edge** — industrial edge-to-cloud telemetry pattern inspired by OT/industrial environments.

The goal is to demonstrate the ability to design, implement, secure, operate, and explain a full-stack hybrid cloud solution with real infrastructure, not only simulated cloud resources.

\---

## 2\. Platform Vision

The main vision of TesteCloud is to show an end-to-end architecture that combines:

1. **Physical infrastructure** — rack, switches, cables, NUC/server, local network devices.
2. **On-prem compute** — Linux, Docker, NGINX, FastAPI, scripts, test runners.
3. **Secure access** — Cloudflare DNS, Cloudflare Tunnel, Cloudflare Access, TLS, identity-aware access.
4. **Google Cloud services** — Pub/Sub, Cloud Run, BigQuery, IAM, VPC, IAP, Cloud Logging, Looker Studio, Firebase, and Vertex AI/Gemini.
5. **Data and analytics** — structured events, test results, application analytics, latency metrics, pass/fail dashboards.
6. **AI-assisted operations** — RAG over documentation, log analysis, probable cause generation, troubleshooting suggestions.
7. **Professional documentation** — architecture diagrams, security model, cost considerations, operational runbooks, troubleshooting logs, and evidence capture.

The platform should communicate the following professional message:

> I designed and built a hybrid cloud portfolio platform using a physical on-prem rack, Linux/Docker workloads, secure external access, Google Cloud event ingestion, BigQuery analytics, operational dashboards, and AI-assisted troubleshooting.

\---

## 3\. High-Level Architecture

```text
┌─────────────────────────────────────────────────────────────┐
│                         Users / Recruiters                  │
│                                                             │
│  Browser │ Android App │ Admin UI │ Public Portfolio │ Demo  │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                    Secure Access Layer                      │
│                                                             │
│  Cloudflare DNS │ Cloudflare Tunnel │ Cloudflare Access      │
│  TLS │ Identity-gated demo access │ API access control       │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                      TesteCloud Rack                        │
│                  Physical On-Prem Infrastructure             │
│                                                             │
│  Home Rack │ NUC │ Docker │ NGINX │ FastAPI │ Switches       │
│  Industrial networking devices │ Lab VLANs │ Local telemetry │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                    Google Cloud Platform                    │
│                                                             │
│  Pub/Sub │ Cloud Run │ BigQuery │ IAM │ VPC │ IAP │ Logging  │
│  Looker Studio │ Firebase │ Vertex AI / Gemini               │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                    Analytics / AI / Portfolio                │
│                                                             │
│  Dashboards │ AI troubleshooting │ Architecture docs         │
│  Evidence logs │ Cost reports │ Security model              │
└─────────────────────────────────────────────────────────────┘
```

\---

## 4\. Core Platform Components

### 4.1 TesteCloud Rack

**TesteCloud Rack** is the physical infrastructure layer of the platform.

It includes:

```text
TesteCloud Rack
│
├── Physical rack
├── Industrial switches / networking devices
├── NUC or mini server
├── Docker host
├── Local reverse proxy
├── Lab cabling
├── Management network
├── Test network
├── Optional sensors
├── Optional UPS / PDU
└── Local monitoring agents
```

The rack acts as:

* A mini data center.
* A hybrid cloud lab.
* An edge computing environment.
* A network validation environment.
* An infrastructure test bench.
* A demo platform for Google Cloud architecture.
* A foundation for AI, monitoring, and automation use cases.

The correct positioning is:

> A physical hybrid infrastructure validation rack used to host, test, secure, monitor, and connect on-prem workloads to Google Cloud.

\---

### 4.2 On-Prem Compute Layer

The on-prem compute layer is based on a Linux host, such as an Intel NUC or mini server.

```text
NUC / Linux Host
│
├── Ubuntu Server
├── Docker Engine
├── Docker Compose
├── NGINX reverse proxy
├── FastAPI backend
├── Local UI
├── Test runners
├── Monitoring agents
├── Log collectors
└── Cloud/event publishers
```

Possible local Docker services:

```text
Local Docker Services
│
├── ui-service
├── api-service
├── rack-test-runner
├── telemetry-collector
├── ai-agent-proxy
├── nginx-proxy
├── mock-industrial-device
└── local-dashboard
```

This layer allows TesteCloud to run workloads locally, expose controlled APIs, collect telemetry, and send structured events to Google Cloud.

\---

### 4.3 Network Layer

The network layer should reflect real infrastructure practices, including management segmentation, application traffic, test traffic, and isolated demo access.

```text
Network Layer
│
├── Management VLAN
├── Application VLAN
├── Test VLAN
├── IoT / Edge VLAN
├── Cloud egress path
├── Mirror port for packet capture
├── Device management interfaces
├── Local DNS / hostnames
└── Firewall / access rules
```

Recommended logical segmentation:

```text
VLAN 10 - Management
  - Switch management
  - NUC management
  - SSH
  - Admin-only access

VLAN 20 - Application
  - Local web apps
  - APIs
  - Docker services

VLAN 30 - Test / Validation
  - iperf tests
  - latency tests
  - device validation
  - simulated traffic

VLAN 40 - Edge / IoT
  - sensors
  - mock PLC/device data
  - telemetry sources

VLAN 50 - Guest / Isolated Demo
  - recruiter demo
  - limited access
```

This segmentation does not need to be implemented fully on day one. It can be documented as a target architecture and implemented progressively.

\---

## 5\. Secure Access Layer

The secure access layer protects the on-prem rack and prevents direct public exposure.

```text
Secure Access Layer
│
├── Domain: testecloud.com
├── Cloudflare DNS
├── Cloudflare Tunnel
├── Cloudflare Access
├── Identity-based access
├── TLS termination
├── API key for lab APIs
└── Optional service tokens
```

Typical external access flow:

```text
Recruiter / Hiring Manager
        │
        ▼
https://demo.testecloud.com
        │
        ▼
Cloudflare Access
        │
        ▼
Cloudflare Tunnel
        │
        ▼
NGINX on NUC
        │
        ▼
Local UI / API
```

This layer demonstrates:

* Zero-trust access pattern.
* No inbound port exposure to the home network.
* Secure hybrid connectivity.
* DNS and domain management.
* Reverse proxy design.
* Identity-aware access for controlled demos.

\---

## 6\. TesteCloud Core Platform

TesteCloud Core Platform is the foundation that connects the physical rack with Google Cloud.

```text
TesteCloud Core Platform
│
├── Local FastAPI API
├── Event publisher
├── Pub/Sub topic
├── Cloud Run consumer
├── BigQuery dataset
├── Looker Studio dashboard
├── IAM service accounts
├── Cloud Logging
├── Budget alerts
└── Evidence documentation
```

Main event flow:

```text
On-Prem Rack / NUC
        │
        ▼
FastAPI Event API
        │
        ▼
Pub/Sub Topic
        │
        ▼
Cloud Run Consumer
        │
        ▼
BigQuery Table
        │
        ▼
Looker Studio Dashboard
```

Example event payload:

```json
{
  "source": "testecloud-rack",
  "device\_id": "sw-core-01",
  "event\_type": "network\_test",
  "test\_name": "latency\_baseline",
  "result": "PASS",
  "latency\_ms": 3.8,
  "packet\_loss\_percent": 0,
  "timestamp": "2026-05-08T14:30:00Z"
}
```

\---

## 7\. Google Cloud Architecture

The Google Cloud layer should be designed around least privilege, serverless processing, event-driven ingestion, private networking, and analytics.

```text
Google Cloud Layer
│
├── IAM
│   ├── testecloud-api-sa
│   ├── testecloud-consumer-sa
│   ├── pubsub-cloudrun-invoker
│   └── least privilege roles
│
├── Networking
│   ├── testecloud-vpc
│   ├── app subnet
│   ├── data subnet
│   ├── Private Google Access
│   ├── private VM
│   └── IAP SSH
│
├── Serverless
│   ├── Cloud Run consumer
│   ├── Cloud Run APIs
│   └── autoscaling
│
├── Eventing
│   ├── Pub/Sub topic
│   ├── Pub/Sub subscription
│   ├── DLQ topic
│   └── retry policy
│
├── Data
│   ├── BigQuery raw events table
│   ├── BigQuery views
│   ├── latency metrics
│   ├── pass/fail metrics
│   └── test history
│
├── Observability
│   ├── Cloud Logging
│   ├── Cloud Monitoring
│   ├── Error analysis
│   └── dashboards
│
└── AI
    ├── Vertex AI / Gemini
    ├── log summarization
    ├── RAG over docs
    └── troubleshooting assistant
```

\---

## 8\. Application and Module Architecture

TesteCloud should be organized as a platform with multiple modules.

```text
TesteCloud
│
├── TesteCloud Core Platform
├── TesteCloud Rack
├── TesteCloud Chat
├── TesteCloud Monitor
├── TesteCloud AI Ops Assistant
└── TesteCloud Edge
```

\---

### 8.1 TesteCloud Chat

TesteCloud Chat is an Android messaging application built as a user-facing cloud application.

```text
TesteCloud Chat
│
├── Android app
├── Firebase Authentication
├── Firestore messages
├── Cloud Run API
├── Pub/Sub event stream
├── BigQuery analytics
└── Looker Studio usage dashboard
```

It demonstrates:

* Android development.
* Firebase Authentication.
* Firestore or real-time messaging.
* Cloud Run APIs.
* Event-driven analytics.
* BigQuery reporting.
* Product-style documentation.

\---

### 8.2 TesteCloud Rack

TesteCloud Rack is the infrastructure validation module.

```text
TesteCloud Rack
│
├── Rack inventory
├── Device inventory
├── Network validation tests
├── Firmware/config tracking
├── Pass/fail test reports
├── Latency / packet loss / throughput
├── Mirror-port troubleshooting workflow
├── BigQuery test history
└── Dashboard
```

Possible test suites:

```text
Test Suites
│
├── Connectivity test
├── VLAN reachability test
├── DNS resolution test
├── API health test
├── Docker container health test
├── Pub/Sub publish test
├── Cloud Run consumer test
├── BigQuery insert validation
├── Latency baseline
├── iperf throughput test
├── Packet loss test
└── Device availability test
```

This module is especially useful for demonstrating infrastructure test automation, Linux command-line skills, network validation, and repeatable pass/fail reporting.

\---

### 8.3 TesteCloud Monitor

TesteCloud Monitor provides observability and operational visibility.

```text
TesteCloud Monitor
│
├── Service uptime
├── API latency
├── Container health
├── Cloud Run logs
├── Pub/Sub backlog
├── BigQuery ingestion status
├── Rack device status
├── Alerting
└── Incident dashboard
```

Possible metrics:

```text
Operational Metrics
│
├── service\_status
├── response\_time\_ms
├── error\_rate
├── event\_count
├── failed\_events
├── dlq\_count
├── pubsub\_delivery\_attempts
├── processing\_delay\_seconds
└── availability\_percentage
```

This module supports SRE-style storytelling and demonstrates how the platform is operated, monitored, and improved.

\---

### 8.4 TesteCloud AI Ops Assistant

TesteCloud AI Ops Assistant is an AI-enabled troubleshooting and operations module.

```text
TesteCloud AI Ops Assistant
│
├── Gemini / Vertex AI
├── Log ingestion
├── Runbook retrieval
├── Architecture documentation search
├── Incident explanation
├── Suggested commands
├── RCA draft generation
└── Troubleshooting recommendations
```

Example input:

```text
Cloud Run consumer is returning 403 after Pub/Sub push delivery.
```

Example output:

```text
Probable cause:
The Pub/Sub push subscription is using an OIDC service account that does not have Cloud Run Invoker permissions.

Recommended validation:
1. Check the subscription push config.
2. Confirm the OIDC service account.
3. Verify Cloud Run IAM policy.
4. Add roles/run.invoker to the Pub/Sub invoker service account.
```

This module demonstrates:

* AI applied to operations.
* RAG over documentation and runbooks.
* Troubleshooting workflows.
* Cloud architecture reasoning.
* Incident response support.

\---

### 8.5 TesteCloud Edge

TesteCloud Edge demonstrates an industrial edge-to-cloud telemetry pattern.

```text
TesteCloud Edge
│
├── Simulated plant data
├── Industrial network telemetry
├── Local processing
├── Event filtering
├── Cloud publishing
├── Store-and-forward logic
├── BigQuery analytics
└── Edge/cloud dashboard
```

Possible use cases:

* Simulated plant telemetry.
* Local event filtering before cloud publishing.
* Anomaly detection at the edge.
* Secure cloud ingestion.
* BigQuery trend analysis.
* Industrial-style dashboarding.

Professional positioning:

> Industrial edge pattern using local processing and secure cloud integration for telemetry, analytics, and operational intelligence.

\---

## 9\. Data Architecture

Recommended BigQuery dataset:

```text
BigQuery Dataset: testecloud\_events
│
├── raw\_events
├── rack\_test\_results
├── app\_events
├── device\_inventory
├── service\_health
├── incident\_events
└── ai\_assistant\_queries
```

### 9.1 Table: `rack\_test\_results`

```text
rack\_id
 device\_id
 test\_suite
 test\_name
 result
 latency\_ms
 packet\_loss\_percent
 throughput\_mbps
 error\_message
 firmware\_version
 config\_hash
 timestamp
```

### 9.2 Table: `service\_health`

```text
service\_name
 environment
 status
 response\_time\_ms
 http\_status
 container\_status
 region
 timestamp
```

### 9.3 Table: `app\_events`

```text
app\_name
 user\_id\_hash
 event\_type
 screen\_name
 device\_type
 timestamp
```

### 9.4 Table: `incident\_events`

```text
incident\_id
 severity
 source
 symptom
 probable\_cause
 resolution
 status
 created\_at
 closed\_at
```

### 9.5 Table: `ai\_assistant\_queries`

```text
query\_id
 user\_id\_hash
 query\_type
 source\_context
 model\_used
 response\_summary
 confidence\_level
 created\_at
```

Sensitive fields should be avoided or hashed. The portfolio should only show sanitized data.

\---

## 10\. Security Architecture

The security model should be documented as a dedicated part of the platform.

```text
Security Model
│
├── No public inbound access to home rack
├── Cloudflare Tunnel for external access
├── Cloudflare Access for identity control
├── API keys for internal APIs
├── Google IAM least privilege
├── Dedicated service accounts
├── Pub/Sub OIDC authentication
├── Cloud Run invoker restriction
├── Private VM with no public IP
├── IAP SSH only
├── Secret handling via env vars / Secret Manager
└── Sanitized evidence for portfolio
```

Main principle:

> Every service has the minimum permissions required to perform its function.

Example service account responsibilities:

```text
testecloud-consumer-sa
  - Can insert into BigQuery
  - Cannot administer project IAM
  - Cannot access unrelated resources

pubsub-cloudrun-invoker
  - Can invoke Cloud Run
  - Cannot write to BigQuery
  - Cannot manage Pub/Sub

testecloud-api-sa
  - Can publish events
  - Limited access only
```

Security topics to document:

* IAM hardening.
* Removal of unnecessary permissions.
* Dedicated service accounts.
* Pub/Sub push authentication with OIDC.
* Cloud Run invoker restrictions.
* Private VM with no external IP.
* IAP-only SSH.
* API key handling.
* Secret handling.
* Public demo access boundaries.

\---

## 11\. Observability Architecture

Observability should cover both local and cloud systems.

```text
Observability
│
├── Local logs
│   ├── NGINX logs
│   ├── Docker logs
│   ├── FastAPI logs
│   └── Test runner logs
│
├── Cloud logs
│   ├── Cloud Run logs
│   ├── Pub/Sub delivery logs
│   ├── BigQuery insert errors
│   └── IAM/audit logs
│
├── Metrics
│   ├── latency
│   ├── event count
│   ├── failure rate
│   ├── DLQ count
│   ├── processing delay
│   └── uptime
│
└── Dashboards
    ├── Looker Studio executive dashboard
    ├── Technical operations dashboard
    └── Rack validation dashboard
```

Recommended operational metrics:

* API latency.
* Event ingestion count.
* Cloud Run processing delay.
* Pub/Sub delivery attempts.
* DLQ message count.
* BigQuery insert errors.
* Rack test pass/fail rate.
* Service uptime.
* Error rate.
* Device availability.

\---

## 12\. End-to-End Flows

### 12.1 Event from the Rack

```text
Device / Script / API on Rack
        │
        ▼
Local FastAPI
        │
        ▼
Pub/Sub Topic
        │
        ▼
Cloud Run Consumer
        │
        ▼
BigQuery
        │
        ▼
Looker Studio
```

\---

### 12.2 Automated Infrastructure Test

```text
Scheduled Test Runner
        │
        ▼
Ping / iperf / API health / Docker health
        │
        ▼
Generate JSON test result
        │
        ▼
Publish to Pub/Sub
        │
        ▼
Store in BigQuery
        │
        ▼
Dashboard pass/fail trends
```

\---

### 12.3 AI Troubleshooting

```text
Logs / Error / Incident
        │
        ▼
AI Ops Assistant
        │
        ▼
RAG over runbooks + architecture docs
        │
        ▼
Probable cause + commands + resolution
        │
        ▼
Incident record saved to BigQuery
```

\---

### 12.4 Mobile App Analytics

```text
TesteCloud Chat Android App
        │
        ▼
Firebase / Cloud Run API
        │
        ▼
Pub/Sub event
        │
        ▼
BigQuery
        │
        ▼
Usage analytics dashboard
```

\---

## 13\. Repository Structure

Recommended repository structure:

```text
testecloud/
│
├── README.md
│
├── docs/
│   ├── 01-executive-summary.md
│   ├── 02-architecture-overview.md
│   ├── 03-hybrid-connectivity.md
│   ├── 04-security-model.md
│   ├── 05-event-driven-platform.md
│   ├── 06-data-analytics.md
│   ├── 07-observability.md
│   ├── 08-cost-considerations.md
│   ├── 09-troubleshooting-log.md
│   └── 10-roadmap.md
│
├── docs/modules/
│   ├── testecloud-chat.md
│   ├── testecloud-rack.md
│   ├── testecloud-monitor.md
│   ├── testecloud-ai-ops.md
│   └── testecloud-edge.md
│
├── docs/architecture/
│   ├── testecloud-platform-architecture.md
│   ├── high-level-architecture.md
│   ├── rack-architecture.md
│   ├── gcp-architecture.md
│   ├── data-flow.md
│   └── security-flow.md
│
├── docs/evidence/
│   ├── cloud-run-identity.md
│   ├── pubsub-oidc-config.md
│   ├── bigquery-results.md
│   ├── iam-hardening.md
│   ├── vpc-private-vm.md
│   └── dashboard-screenshots.md
│
├── apps/
│   ├── testecloud-chat/
│   ├── testecloud-ui/
│   ├── testecloud-api/
│   └── testecloud-ai-ops/
│
├── services/
│   ├── event-consumer/
│   ├── rack-test-runner/
│   ├── telemetry-collector/
│   └── ai-assistant-api/
│
├── infrastructure/
│   ├── gcp/
│   ├── cloudflare/
│   ├── docker/
│   └── scripts/
│
└── dashboards/
    ├── bigquery-views.sql
    ├── looker-studio-notes.md
    └── sample-metrics.md
```

\---

## 14\. Implementation Phases

### Phase 1 — Brand and Base Architecture

Goal: Define TesteCloud as a coherent platform.

Deliverables:

```text
README.md
Architecture overview
High-level diagram
Security model
Current state vs target state
```

\---

### Phase 2 — Formalize TesteCloud Rack

Goal: Convert the physical rack into a documented portfolio asset.

Deliverables:

```text
Rack inventory
Network diagram
Device naming convention
VLAN plan
Cable labeling plan
Before/after rack cleanup photos
Basic rack validation tests
```

\---

### Phase 3 — TesteCloud Core Platform

Goal: Stabilize the hybrid event pipeline.

Deliverables:

```text
On-prem API
Pub/Sub topic
Cloud Run consumer
BigQuery table
Looker dashboard
IAM evidence
DLQ evidence
Cost notes
```

\---

### Phase 4 — TesteCloud Chat

Goal: Demonstrate a user-facing cloud application.

Deliverables:

```text
Android app
Firebase Authentication
Firestore/messages
Cloud Run API integration
Analytics events
Dashboard
Architecture doc
```

\---

### Phase 5 — TesteCloud Rack Test Automation

Goal: Demonstrate infrastructure validation and test automation.

Deliverables:

```text
Python/Bash test runner
Pass/fail report
Device inventory
Latency/throughput tests
Config/firmware tracking
BigQuery test history
Dashboard
```

\---

### Phase 6 — TesteCloud AI Ops Assistant

Goal: Demonstrate AI-assisted cloud operations.

Deliverables:

```text
Gemini/Vertex AI integration
RAG over docs
Log analysis
Troubleshooting assistant
Incident summaries
Runbook generator
```

\---

## 15\. Interview Positioning

Recommended English pitch:

> I built TesteCloud as a hybrid cloud portfolio platform using a physical on-prem rack, Linux/Docker workloads, secure Cloudflare-based access, Google Cloud event ingestion through Pub/Sub and Cloud Run, BigQuery analytics, Looker Studio dashboards, and AI-assisted operations. The platform supports multiple modules, including a mobile chat app, rack validation automation, infrastructure monitoring, and edge-to-cloud industrial telemetry patterns.

Recommended Spanish pitch:

> Construí TesteCloud como una plataforma híbrida de portfolio usando un rack físico on-prem, workloads Linux/Docker, acceso seguro con Cloudflare, ingestión de eventos en Google Cloud con Pub/Sub y Cloud Run, analítica en BigQuery, dashboards en Looker Studio y operaciones asistidas por AI. La plataforma soporta múltiples módulos como una app móvil de chat, automatización de validación del rack, monitoreo de infraestructura y patrones industriales edge-to-cloud.

\---

## 16\. Final Target Architecture Summary

```text
TesteCloud
│
├── Physical Layer
│   └── TesteCloud Rack
│
├── Compute Layer
│   └── NUC + Linux + Docker
│
├── Connectivity Layer
│   └── Cloudflare Tunnel + Access
│
├── Platform Layer
│   └── FastAPI + Pub/Sub + Cloud Run + BigQuery
│
├── Security Layer
│   └── IAM + service accounts + IAP + least privilege
│
├── Data Layer
│   └── raw events + test results + app analytics
│
├── Observability Layer
│   └── logs + metrics + dashboards + DLQ
│
├── AI Layer
│   └── Gemini/Vertex AI + RAG + troubleshooting
│
└── Application Layer
    ├── TesteCloud Chat
    ├── TesteCloud Rack
    ├── TesteCloud Monitor
    ├── TesteCloud Edge
    └── TesteCloud AI Ops Assistant
```

\---

## 17\. Next Recommended Document

The next recommended document is:

```text
docs/architecture/rack-architecture.md
```

That document should describe the physical rack, device inventory, cabling, VLANs, management network, test network, validation flows, and planned improvements.

