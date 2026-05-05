# TestedCloud Core Platform

## 1\. Executive Summary

TestedCloud Core Platform is a hybrid cloud lab that demonstrates secure integration between an on-prem Docker-based application stack and Google Cloud services.

The current platform sends events from an on-prem UI/API to Google Cloud Pub/Sub. Events are processed by a Cloud Run consumer and stored in BigQuery for analytics and dashboarding.

The platform also includes private cloud networking, least-privilege IAM, authenticated service-to-service communication, Cloudflare Access protection, and dead-letter queue handling.

## 2\. Technical Scenario

Many industrial and enterprise environments still run workloads on-premises due to latency, security, operational, regulatory, or legacy constraints.

TestedCloud simulates a realistic modernization pattern:

* Keep selected workloads on-premises
* Publish selected events or telemetry to the cloud
* Process events with serverless services
* Store structured events in an analytics platform
* Visualize operational data
* Apply least-privilege IAM
* Prepare the architecture for future AI/ML analytics

This mirrors common hybrid cloud and industrial modernization patterns, especially for OT/IT environments.

## 3\. Current Pipeline

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
Looker Studio / Dashboard Views
```

## 4\. On-prem Environment

The on-prem portion runs on an Intel NUC with Ubuntu Server and Docker Compose.

Current host:

* Hostname: `ubuserver`
* Hardware: `Intel NUC7i3BNH`
* OS: `Ubuntu 24.04.4 LTS`
* Local project path: `/home/dario/testedcloud-lab`
* Local access: `http://localhost:8082`
* Protected external access: `https://ui.testedcloud.com`

Docker Compose services:

* `testedcloud-api`
* `testedcloud-ui`
* `testedcloud-nginx`

External access is protected by Cloudflare Access.

A previous public router port forwarding rule to port `8082` was removed because it bypassed Cloudflare Access. The lab is now accessed through the protected domain instead of direct public IP exposure.

## 5\. Google Cloud Environment

Google Cloud project:

* Project ID: `majestic-layout-255620`
* Project number: `644725546932`
* Primary region: `us-central1`

Core services:

* Pub/Sub
* Cloud Run
* BigQuery
* Looker Studio
* VPC
* IAP
* IAM Service Accounts

## 6\. Pub/Sub Design

Pub/Sub is used as the ingestion layer between the on-prem system and the cloud processing layer.

Topics:

* `testedcloud-events`
* `testedcloud-events-dlq`

Subscriptions:

* `testedcloud-consumer-sub`
* `testedcloud-events-dlq-sub`

The main subscription uses push delivery to Cloud Run.

The dead-letter queue was validated by publishing a malformed payload:

```json
{"bad\_payload": true}
```

After five failed delivery attempts, the message was sent to the DLQ.

This validates that the architecture has basic failure handling for malformed messages or processing failures.

## 7\. Cloud Run Design

Cloud Run hosts the event consumer:

```text
testedcloud-consumer
```

Cloud Run URL:

```text
https://testedcloud-consumer-644725546932.us-central1.run.app
```

The consumer receives Pub/Sub push messages, validates the payload, processes valid events, and writes them to BigQuery.

The service was migrated away from the default Compute Engine service account and now uses a dedicated service account:

```text
testedcloud-consumer-sa@majestic-layout-255620.iam.gserviceaccount.com
```

This improves the security posture by reducing dependency on broad default identities.

## 8\. BigQuery Design

BigQuery stores processed events in:

```text
testedcloud\_events.hybrid\_events
```

Current schema:

|Field|Type|Purpose|
|-|-|-|
|`event\_id`|STRING|Unique event identifier|
|`received\_at`|TIMESTAMP|Time when the event was received or created|
|`source`|STRING|Source system|
|`event\_type`|STRING|Event category|
|`message`|STRING|Event message|
|`origin`|STRING|Origin environment or workload|
|`processed\_at`|TIMESTAMP|Time when Cloud Run processed the event|
|`user\_email`|STRING|User identity when available|

Current views:

* `v\_dashboard\_events`
* `v\_dashboard\_events\_v2`
* `v\_latency\_metrics`
* `v\_latency\_metrics\_v2`

These views support dashboarding, event analysis, and latency analysis.

## 9\. Dashboard / Analytics Layer

Looker Studio is used as the visualization layer for the data stored in BigQuery.

The dashboard is intended to demonstrate:

* Event volume over time
* Events by source
* Events by origin
* Events by event type
* Processing latency
* p50, p95, and p99 latency metrics
* Pipeline behavior after IAM hardening and DLQ validation

Current observed metrics:

|Metric|Value|
|-|-|
|Total events|72+|
|Web UI events|63|
|On-prem NUC events|3|
|Private GCP VM events|2|
|p50 latency|0 seconds|
|p95 latency|5 seconds|
|p99 latency|5 seconds|

## 10\. Networking Design

The cloud networking layer uses a custom VPC:

```text
testedcloud-vpc
```

Subnets:

|Subnet|CIDR|Region|Purpose|
|-|-|-|-|
|`testedcloud-subnet-app`|`10.10.1.0/24`|`us-central1`|Application tier|
|`testedcloud-subnet-data`|`10.10.2.0/24`|`us-central1`|Data tier|

Private Google Access is enabled on both subnets.

A private VM was created without an external IP:

```text
testedcloud-vm-app-test
```

VM details:

* Zone: `us-central1-a`
* Machine type: `e2-micro`
* Internal IP: `10.10.1.2`
* External IP: none

The VM is accessed through IAP SSH, using the firewall rule:

```text
testedcloud-allow-iap-ssh
```

Allowed source range:

```text
35.235.240.0/20
```

Allowed port:

```text
tcp:22
```

This demonstrates secure administrative access without exposing SSH directly to the public internet.

An internal firewall rule is also configured:

```text
testedcloud-allow-internal
```

This supports internal communication inside the custom VPC.

## 11\. IAM Design

The platform uses dedicated service accounts instead of relying on the default Compute Engine service account.

Current service accounts:

|Service Account|Purpose|
|-|-|
|`testedcloud-api-sa@majestic-layout-255620.iam.gserviceaccount.com`|API/on-prem publishing identity|
|`testedcloud-consumer-sa@majestic-layout-255620.iam.gserviceaccount.com`|Cloud Run runtime identity|
|`pubsub-cloudrun-invoker@majestic-layout-255620.iam.gserviceaccount.com`|Pub/Sub OIDC identity to invoke Cloud Run|

Current IAM model:

### `testedcloud-api-sa`

Assigned responsibilities:

* Publish events to Pub/Sub
* Read BigQuery metadata/views where needed
* Run BigQuery jobs where needed

Roles:

* Pub/Sub Publisher
* BigQuery Data Viewer
* BigQuery Job User

### `testedcloud-consumer-sa`

Assigned responsibilities:

* Execute the Cloud Run consumer
* Read Pub/Sub push payloads
* Write processed events to BigQuery

Roles:

* BigQuery Data Editor
* BigQuery Job User
* Pub/Sub Subscriber

### `pubsub-cloudrun-invoker`

Assigned responsibilities:

* Used by Pub/Sub push subscription to invoke Cloud Run through OIDC

Role:

* Cloud Run Invoker on `testedcloud-consumer`

Important hardening completed:

* Cloud Run was migrated from the default Compute Engine service account to `testedcloud-consumer-sa`
* The legacy Cloud Run service `project1` was deleted
* `roles/editor` was removed from the default Compute Engine service account
* `roles/bigquery.dataEditor` was removed from the default Compute Engine service account
* Validation showed no remaining IAM bindings for `644725546932-compute@developer.gserviceaccount.com`
* The pipeline continued working after IAM hardening

## 12\. Security Controls

Implemented controls:

* Cloudflare Access protects external access to `ui.testedcloud.com`
* Direct public IP exposure was removed
* Cloud Run uses a dedicated runtime service account
* Pub/Sub invokes Cloud Run using OIDC
* Private VM has no external IP
* SSH access is restricted through IAP
* Default Compute Engine service account no longer has broad roles
* Pub/Sub DLQ is configured for failed message handling
* Custom VPC separates application and data subnets
* Private Google Access is enabled on private subnets

Known improvements:

* Add `/api/health`
* Move API key out of the frontend
* Add stronger secret management
* Add Cloud Monitoring alerts
* Add budget alerts
* Improve dashboard documentation
* Consider Secret Manager for sensitive runtime configuration
* Consider Cloud Armor or additional edge controls if public APIs are introduced

## 13\. Reliability and Failure Handling

The current architecture includes basic reliability mechanisms:

* Pub/Sub decouples the on-prem producer from the cloud consumer
* Cloud Run can scale independently from the on-prem application
* BigQuery provides durable analytical storage
* DLQ captures messages that cannot be processed successfully
* Cloud Run logs provide operational visibility into message processing

DLQ behavior was tested by publishing a malformed event payload.

Expected behavior:

1. Pub/Sub delivers the malformed message to Cloud Run.
2. Cloud Run rejects or fails to process the message.
3. Pub/Sub retries delivery.
4. After the configured maximum delivery attempts, the message is routed to the DLQ.
5. The DLQ subscription can be inspected to analyze failed messages.

Validated result:

* The bad payload reached the DLQ after five attempts.

## 14\. Validated Test Scenarios

The following scenarios have been validated:

|Test|Result|
|-|-|
|Web UI event to BigQuery|Successful|
|Manual on-prem NUC event to BigQuery|Successful|
|Private GCP VM event to BigQuery|Successful|
|Cloud Run processing logs|Successful|
|IAM hardening without breaking pipeline|Successful|
|DLQ with malformed payload|Successful|
|Cloudflare Access protection|Successful|
|Direct public IP exposure removed|Successful|
|Cloud Run logs showing POST 204|Successful|
|Cloud Run logs showing `PIPELINE\_EVENT\_PROCESSED`|Successful|

## 15\. Troubleshooting Already Documented

The following issues were identified and resolved during the build:

### 1\. Public exposure on port 8082

Problem:

* The lab was reachable through direct public IP and port forwarding.
* This bypassed Cloudflare Access.

Resolution:

* Removed router port forwarding.
* Confirmed that direct public IP access no longer works.
* External access now goes through `ui.testedcloud.com` protected by Cloudflare Access.

### 2\. Cloudflare Access session expiration

Problem:

* Browser requests appeared to fail with symptoms similar to CORS errors.
* Root cause was an expired Cloudflare Access session.

Resolution:

* Re-authenticated through Cloudflare Access.
* Confirmed the UI worked again.

### 3\. Cloud Run using default Compute Engine service account

Problem:

* Cloud Run was originally using the default Compute Engine service account.

Resolution:

* Created and used `testedcloud-consumer-sa`.
* Migrated Cloud Run to the dedicated service account.

### 4\. Broad IAM permissions on default Compute Engine service account

Problem:

* The default Compute Engine service account had broad permissions, including Editor and BigQuery Data Editor.

Resolution:

* Removed `roles/editor`.
* Removed `roles/bigquery.dataEditor`.
* Verified no remaining bindings for the default Compute Engine service account.
* Confirmed the pipeline still worked.

### 5\. DLQ validation

Problem:

* Needed to validate failed message handling.

Resolution:

* Published a malformed payload.
* Confirmed the message reached the DLQ after five attempts.

### 6\. Missing health endpoint

Problem:

* `/api/health` does not exist yet.

Resolution:

* Pending improvement.

### 7\. Hardcoded frontend API key

Problem:

* The API key is currently hardcoded in the frontend.

Resolution:

* Pending improvement.
* Candidate options include backend-only validation, environment-based configuration, Cloudflare Access identity checks, or Secret Manager for server-side secrets.

### 8\. DNS and domain separation

Problem:

* Needed to separate public portfolio access from protected lab access.

Resolution / target design:

* `testedcloud.com` = public portfolio landing page
* `ui.testedcloud.com` = protected lab UI
* `api.testedcloud.com` = optional protected API endpoint in the future

## 16\. Current Limitations

Current known limitations:

* `/api/health` endpoint does not exist yet
* API key is currently hardcoded in the frontend
* Monitoring and alerting are not fully configured
* Budget alerts are not configured yet
* The public landing page is not complete yet
* Architecture diagrams still need to be created
* The dashboard needs final polishing for portfolio presentation
* API endpoint strategy is not finalized
* Secrets management should be improved before any broader exposure
* The industrial telemetry module is not implemented yet
* TestedChat is planned but not yet implemented

## 17\. Cost Considerations

Current cost posture is intentionally lightweight.

The lab uses cost-conscious services and configurations:

* Cloud Run for scale-to-zero serverless processing
* Pub/Sub for low-volume event ingestion
* BigQuery for analytical storage and queries
* e2-micro VM for private networking tests
* Limited region scope in `us-central1`
* Docker on an existing on-prem Intel NUC
* Cloudflare Access for protected external access

Important cost considerations:

* BigQuery query costs should be controlled with optimized views and limited dashboard refresh frequency
* Cloud Run costs should remain low while traffic is minimal
* Pub/Sub costs should remain low during lab-scale event ingestion
* Budget alerts should be configured to prevent unexpected spend
* Long-running VMs should be minimized or stopped when not needed
* Future Vertex AI experiments should be isolated and budget-controlled

Recommended budget alert strategy:

|Budget Level|Purpose|
|-|-|
|50%|Early awareness|
|80%|Action threshold|
|100%|Hard review point|

## 18\. Roadmap

### Phase 1 — Core Platform Completion

* Finalize documentation
* Create architecture diagrams
* Improve dashboard
* Add `/health` endpoint
* Add monitoring and alerting
* Add budget alerts
* Prepare public landing page

### Phase 2 — Public Portfolio Site

Create a public portfolio landing page at:

```text
https://testedcloud.com
```

The protected lab UI will remain under:

```text
https://ui.testedcloud.com
```

### Phase 3 — TestedChat Release 1

Build an Android/Firebase messaging application to demonstrate:

* Firebase Authentication
* Firestore real-time messaging
* Basic user profiles
* Chat conversations
* Mobile-first architecture
* Privacy and data safety foundations

### Phase 4 — TestedChat Release 2

Extend TestedChat with cloud analytics:

* Cloud Run APIs
* Pub/Sub event flow
* BigQuery analytics
* Looker Studio dashboards
* IAM/service account hardening
* Observability and auditability

### Phase 5 — TestedChat Play Store Closed Testing

Prepare the app for Google Play closed testing:

* App signing
* Data safety form
* Privacy policy
* Testing track
* Release notes
* Crash/usage monitoring

### Phase 6 — Industrial Telemetry / SINEC NMS Module

Add an industrial telemetry module based on SINEC NMS.

Potential goals:

* Pull data from SINEC NMS REST API
* Normalize asset and network telemetry
* Send selected telemetry to Pub/Sub
* Store and analyze events in BigQuery
* Visualize network health in Looker Studio

### Phase 7 — Industrial Network Telemetry Demo

Extend the lab with industrial networking devices and telemetry patterns.

Potential sources:

* Ruggedcom
* SCALANCE
* SNMP
* Syslog
* Device inventory
* Interface status
* Network events
* Alarm/event data

### Phase 8 — BGP / Hybrid Connectivity Lab

Build a routing-focused hybrid connectivity lab.

Potential goals:

* Learn and demonstrate BGP concepts
* Simulate hybrid connectivity patterns
* Compare VPN-style connectivity with higher-end interconnect patterns
* Document routing, segmentation, and security trade-offs

### Phase 9 — Vertex AI / Advanced Analytics

Add advanced analytics and AI/ML capabilities.

Potential goals:

* Use BigQuery data as an analytics source
* Explore anomaly detection use cases
* Build simple AI-assisted operational insights
* Document when AI should run on-prem, at the edge, or in the cloud

## 19\. Interview Narrative

I built TestedCloud to translate my background in industrial networking, secure infrastructure, and on-prem systems into practical Google Cloud architecture patterns.

The platform demonstrates how on-prem workloads can integrate with cloud-native services for event ingestion, serverless processing, analytics, observability, IAM hardening, private networking, and operational troubleshooting.

I designed the lab to reflect real customer conversations around hybrid cloud adoption, secure connectivity, operational visibility, modernization, and cost-conscious architecture.

The project is being extended with TestedChat to demonstrate Firebase, serverless mobile backends, product analytics, and Google Play readiness.

## 20\. Final Positioning

TestedCloud demonstrates my ability to bridge industrial infrastructure and modern cloud-native application development on Google Cloud.

It connects my existing strengths in industrial networking, secure infrastructure, troubleshooting, and systems engineering with practical Google Cloud architecture patterns, including Pub/Sub, Cloud Run, BigQuery, IAM, VPC design, private access, observability, and analytics.

