# TestedCloud Validated Tests

## 1\. Purpose

This document summarizes the main validations completed for the TestedCloud Core Platform.

The goal is to provide a single executive index of what has been tested, what evidence exists, and why each validation matters for the portfolio.

This file complements the detailed documentation stored in:

* `docs/core-platform.md`
* `docs/architecture/hybrid-architecture.md`
* `docs/security/iam-hardening.md`
* `docs/security/cloudflare-access.md`
* `docs/operations/dlq-validation.md`
* `docs/operations/troubleshooting-log.md`
* `docs/cost/cost-considerations.md`
* `docs/cost/budget-alerts.md`
* `docs/evidence/README.md`

## 2\. Validation Summary

|#|Validation|Status|Evidence|
|-|-|-|-|
|1|Core pipeline: on-prem UI/API to Pub/Sub to Cloud Run to BigQuery|Validated|`bigquery-events-sample.txt`, `cloud-run-logs-recent.txt`|
|2|Cloud Run uses dedicated service account|Validated|`cloud-run-service-identity.txt`|
|3|Pub/Sub push subscription uses OIDC authentication|Validated|`pubsub-push-oidc-config.txt`|
|4|Pub/Sub subscription has DLQ policy|Validated|`pubsub-subscription-dlq-config.txt`|
|5|Malformed message reached DLQ after retry exhaustion|Validated|`dlq-validation.md`, optional `dlq-pull-output.txt`|
|6|Default Compute Engine service account IAM cleanup|Validated|`iam-policy-after-hardening.txt`|
|7|BigQuery event storage|Validated|`bigquery-events-sample.txt`|
|8|BigQuery latency metrics view|Validated|`bigquery-latency-metrics.txt`|
|9|Private VM has private networking configuration|Validated|`private-vm-config.txt`|
|10|IAP SSH firewall rule exists|Validated|`iap-firewall-rule.txt`|
|11|Cloudflare Access protects `ui.testedcloud.com`|Validated|`cloudflare-access-validation.txt`|
|12|Public port forwarding bypass was removed|Validated|`cloudflare-access-validation.txt`, `troubleshooting-log.md`|
|13|Billing is enabled for the project|Validated|`billing-project-link.txt`|
|14|Budget alert strategy documented|Documented|`budget-alerts.md`, `budget-alerts-config.txt`|
|15|Cost considerations documented|Documented|`cost-considerations.md`|

## 3\. End-to-End Pipeline Validation

### Test Objective

Validate that events generated from the on-prem UI/API can reach Google Cloud and be stored in BigQuery.

### Tested Flow

```text
On-prem UI/API
    |
    v
Pub/Sub topic: testedcloud-events
    |
    v
Push subscription: testedcloud-consumer-sub
    |
    v
Cloud Run: testedcloud-consumer
    |
    v
BigQuery: testedcloud\_events.hybrid\_events
```

### Status

```text
Validated
```

### Evidence

```text
docs/evidence/bigquery-events-sample.txt
```

### Validation Result

The BigQuery sample confirms that events from sources such as `web-ui` and `onprem-nuc` were processed and stored in BigQuery.

The sanitized sample includes:

* `event\_id`
* `received\_at`
* `source`
* `event\_type`
* `origin`
* `processed\_at`
* `user\_email`

User email values were sanitized before committing evidence.

### Portfolio Value

This proves the main hybrid cloud pipeline works end-to-end.

## 4\. Cloud Run Runtime Identity Validation

### Test Objective

Validate that Cloud Run no longer uses the default Compute Engine service account and instead uses a dedicated service account.

### Expected Runtime Identity

```text
testedcloud-consumer-sa@majestic-layout-255620.iam.gserviceaccount.com
```

### Status

```text
Validated
```

### Evidence

```text
docs/evidence/cloud-run-service-identity.txt
```

### Validation Result

Cloud Run is running as:

```text
testedcloud-consumer-sa@majestic-layout-255620.iam.gserviceaccount.com
```

### Portfolio Value

This demonstrates IAM hardening and least-privilege service identity design.

## 5\. Pub/Sub OIDC Push Authentication Validation

### Test Objective

Validate that Pub/Sub invokes Cloud Run using OIDC authentication through a dedicated invoker service account.

### Expected Invoker Identity

```text
pubsub-cloudrun-invoker@majestic-layout-255620.iam.gserviceaccount.com
```

### Status

```text
Validated
```

### Evidence

```text
docs/evidence/pubsub-push-oidc-config.txt
```

### Validation Result

The Pub/Sub push subscription includes:

```text
pushConfig:
  oidcToken:
    serviceAccountEmail: pubsub-cloudrun-invoker@majestic-layout-255620.iam.gserviceaccount.com
  pushEndpoint: https://testedcloud-consumer-644725546932.us-central1.run.app/
```

### Portfolio Value

This demonstrates secure service-to-service invocation rather than relying on unauthenticated access.

## 6\. Pub/Sub DLQ Configuration Validation

### Test Objective

Validate that the Pub/Sub subscription has a dead-letter policy configured.

### Expected DLQ Configuration

```text
deadLetterPolicy:
  deadLetterTopic: projects/majestic-layout-255620/topics/testedcloud-events-dlq
  maxDeliveryAttempts: 5
```

### Status

```text
Validated
```

### Evidence

```text
docs/evidence/pubsub-subscription-dlq-config.txt
```

### Validation Result

The subscription is configured to send failed messages to:

```text
testedcloud-events-dlq
```

after:

```text
5 delivery attempts
```

### Portfolio Value

This demonstrates reliability thinking and failure-path design for an event-driven architecture.

## 7\. DLQ Failure-Path Validation

### Test Objective

Validate that malformed messages are retried and eventually sent to the DLQ.

### Test Payload

```json
{"bad\_payload": true}
```

### Expected Behavior

```text
Malformed payload
    |
    v
Pub/Sub retry
    |
    v
Max delivery attempts reached
    |
    v
Dead-letter topic
```

### Status

```text
Validated
```

### Evidence

Primary documentation:

```text
docs/operations/dlq-validation.md
```

Optional evidence file:

```text
docs/evidence/dlq-pull-output.txt
```

### Validation Result

The malformed payload reached the DLQ after five attempts.

### Portfolio Value

This proves that the project was tested beyond the happy path.

## 8\. IAM Cleanup Validation

### Test Objective

Validate that the default Compute Engine service account no longer has remaining IAM bindings.

### Default Compute Engine Service Account

```text
644725546932-compute@developer.gserviceaccount.com
```

### Status

```text
Validated
```

### Evidence

```text
docs/evidence/iam-policy-after-hardening.txt
```

### Expected Result

```text
Listed 0 items.
```

### Portfolio Value

This demonstrates least-privilege hardening and removal of broad default permissions.

## 9\. BigQuery Event Storage Validation

### Test Objective

Validate that processed events are stored in BigQuery.

### Table

```text
testedcloud\_events.hybrid\_events
```

### Status

```text
Validated
```

### Evidence

```text
docs/evidence/bigquery-events-sample.txt
```

### Validation Result

The evidence shows processed events from:

* `web-ui`
* `onprem-nuc`

with event types such as:

* `button\_click`
* `post\_editor\_removal\_test`
* `verification\_test`

### Portfolio Value

This proves that Cloud Run successfully writes processed events into the analytical storage layer.

## 10\. BigQuery Latency Metrics Validation

### Test Objective

Validate that latency metrics are available through BigQuery views.

### View

```text
testedcloud\_events.v\_latency\_metrics\_v2
```

### Status

```text
Validated
```

### Evidence

```text
docs/evidence/bigquery-latency-metrics.txt
```

### Observed Metrics

The captured evidence includes:

|Metric|Observed Value|
|-|-|
|p50 latency|424 ms|
|p95 latency|5321 ms|
|p99 latency|5890 ms|

### Portfolio Value

This demonstrates analytics and observability maturity beyond simply storing raw events.

## 11\. Private VM Configuration Validation

### Test Objective

Validate that the private VM exists in the custom VPC and uses an internal IP.

### VM

```text
testedcloud-vm-app-test
```

### Status

```text
Validated
```

### Evidence

```text
docs/evidence/private-vm-config.txt
```

### Observed Configuration

Captured evidence shows:

* Machine type: `e2-micro`
* VPC: `testedcloud-vpc`
* Subnet: `testedcloud-subnet-app`
* Internal IP: `10.10.1.2`
* Zone: `us-central1-a`

### Portfolio Value

This demonstrates private networking design and secure VM placement without relying on public exposure.

## 12\. IAP SSH Firewall Rule Validation

### Test Objective

Validate that IAP SSH firewall access is configured.

### Firewall Rule

```text
testedcloud-allow-iap-ssh
```

### Status

```text
Validated
```

### Evidence

```text
docs/evidence/iap-firewall-rule.txt
```

### Observed Configuration

Captured evidence confirms:

```text
source range: 35.235.240.0/20
protocol: tcp
port: 22
direction: INGRESS
network: testedcloud-vpc
```

### Portfolio Value

This demonstrates secure administrative access to private cloud resources without exposing SSH publicly.

## 13\. Cloudflare Access Validation

### Test Objective

Validate that the protected lab UI is accessed through Cloudflare Access and that direct public port forwarding was removed.

### Protected Application

```text
https://ui.testedcloud.com
```

### Status

```text
Validated
```

### Evidence

```text
docs/evidence/cloudflare-access-validation.txt
```

### Validation Result

Captured evidence states:

* Access through protected domain: successful
* Cloudflare Access authentication required: yes
* Direct public port forwarding to local port `8082`: removed
* Direct public IP access to local lab port `8082`: not available
* Protected UI remained functional after removing public port forwarding

### Portfolio Value

This demonstrates practical access security and remediation of a bypass path.

## 14\. Billing Project Validation

### Test Objective

Validate that the Google Cloud project has billing enabled while keeping billing account details sanitized.

### Project

```text
majestic-layout-255620
```

### Status

```text
Validated
```

### Evidence

```text
docs/evidence/billing-project-link.txt
```

### Validation Result

Captured evidence shows:

```text
billingEnabled: true
billingAccountName: billingAccounts/XXXXXX-XXXXXX-XXXXXX
projectId: majestic-layout-255620
```

### Portfolio Value

This supports the cost governance documentation without exposing the real billing account ID.

## 15\. Budget Alert Evidence

### Test Objective

Document the planned or configured budget alert strategy.

### Budget Name

```text
TestedCloud Lab Budget
```

### Status

```text
Planned / To be configured
```

### Evidence

```text
docs/evidence/budget-alerts-config.txt
```

### Current Evidence

```text
Amount: $20/month
Thresholds: 50%, 80%, 100%
Notifications: Email enabled
```

### Portfolio Value

This demonstrates financial governance and cost-awareness.

## 16\. Validation Coverage Map

|Area|Validated?|Notes|
|-|-|-|
|On-prem UI/API|Yes|Events successfully published|
|Pub/Sub ingestion|Yes|Topic and push subscription working|
|Cloud Run processing|Yes|Events processed and written to BigQuery|
|BigQuery storage|Yes|Events visible in table|
|BigQuery latency views|Yes|p50/p95/p99 captured|
|DLQ configuration|Yes|Dead-letter policy configured|
|DLQ behavior|Yes|Bad payload reached DLQ|
|IAM hardening|Yes|Dedicated SAs and default SA cleanup|
|Cloudflare Access|Yes|Protected path validated|
|Public port forwarding removal|Yes|Bypass path removed|
|Private VPC|Yes|Private VM in custom VPC|
|IAP SSH firewall|Yes|IAP source range and TCP/22 configured|
|Billing enabled|Yes|Billing evidence sanitized|
|Budget alerts|Planned / documented|Evidence file created|
|Monitoring alerts|Not yet|Future improvement|
|`/api/health` endpoint|Not yet|Future improvement|
|Secret management improvement|Not yet|Future improvement|

## 17\. Current Open Validation Items

The following items are not yet fully validated or implemented:

|Item|Status|Next Step|
|-|-|-|
|`/api/health` endpoint|Not implemented|Add API endpoint and validate with curl|
|Monitoring alerts|Not configured|Add alerts for Cloud Run 5xx, Pub/Sub backlog, DLQ count|
|Budget alert actual console config|Planned / partially documented|Configure in Console and update evidence status|
|Secret management|Needs improvement|Move sensitive values out of frontend-exposed code|
|Dashboard final polish|In progress|Improve Looker Studio presentation|
|Public landing page|Not complete|Build `testedcloud.com` portfolio page|
|Industrial telemetry module|Planned|Add SINEC NMS / SNMP / syslog later|
|BGP hybrid lab|Planned|Build routing-focused lab later|
|Vertex AI analytics|Planned|Add after budget alerts and monitoring controls|

## 18\. Interview Explanation

A concise way to explain the validation work:

> I created a validation index for TestedCloud so the project is backed by evidence, not only architecture diagrams. The validations include the end-to-end pipeline from on-prem UI/API to Pub/Sub, Cloud Run, and BigQuery; IAM hardening with dedicated service accounts; Pub/Sub OIDC authentication; DLQ failure handling; private VM and IAP access; Cloudflare Access protection; and cost governance evidence.

A more technical version:

> Each major architecture decision has a corresponding validation artifact. For example, Cloud Run runtime identity is backed by a service account output, Pub/Sub OIDC and DLQ behavior are backed by subscription configuration, BigQuery processing is backed by query output, private networking is backed by VM and firewall outputs, and Cloudflare Access is backed by a sanitized validation file. This makes the portfolio more defensible in a technical interview.

## 19\. Customer Engineer Relevance

This validation index is relevant to Customer Engineer and Cloud Architect roles because it demonstrates the ability to:

* Validate architecture decisions with evidence
* Test both happy-path and failure-path behavior
* Document operational outcomes
* Explain security improvements clearly
* Connect implementation evidence to customer-facing architecture narratives
* Think across networking, IAM, serverless, analytics, cost, and operations
* Communicate technical work in a structured and reviewable way

## 20\. Final Positioning

The validated tests show that TestedCloud is not only a conceptual architecture.

It is a working hybrid cloud lab with documented evidence across event ingestion, serverless processing, analytics, IAM, private networking, secure access, troubleshooting, and cost governance.

This strengthens the portfolio by making the project verifiable, explainable, and defensible.

