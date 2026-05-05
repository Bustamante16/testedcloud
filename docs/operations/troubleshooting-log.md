# TestedCloud Troubleshooting Log

## 1\. Purpose

This document captures the main troubleshooting events identified and resolved during the TestedCloud Core Platform build.

The purpose is to document not only the final architecture, but also the operational learning process behind the lab. This is important because real cloud architecture work includes debugging, validating assumptions, identifying root causes, and improving the system after issues are discovered.

This document is part of the TestedCloud portfolio documentation and demonstrates practical troubleshooting across hybrid networking, Cloudflare Access, Cloud Run, IAM, Pub/Sub, BigQuery, DNS, and application design.

## 2\. Troubleshooting Summary

|#|Issue|Area|Status|
|-|-|-|-|
|1|Public port forwarding bypassed Cloudflare Access|Security / Networking|Resolved|
|2|Cloudflare Access session expiration appeared as CORS issue|Access / Browser / Security|Resolved|
|3|Cloud Run used default Compute Engine service account|IAM / Cloud Run|Resolved|
|4|Default Compute Engine service account had broad roles|IAM / Security|Resolved|
|5|DLQ needed validation with malformed payload|Pub/Sub / Reliability|Resolved|
|6|`/api/health` endpoint does not exist yet|API / Operations|Open|
|7|API key is currently hardcoded in frontend|Application Security|Open|
|8|DNS/root/subdomain separation needed clarification|DNS / Portfolio Design|Resolved / Target Design Defined|

## 3\. Issue 1 — Public Port Forwarding Bypassed Cloudflare Access

### Problem

The on-prem lab was accessible through direct public IP exposure because a router port forwarding rule exposed the local lab port:

```text
8082
```

This created a security issue because traffic could reach the lab directly without passing through Cloudflare Access.

### Impact

The protected domain:

```text
https://ui.testedcloud.com
```

was protected by Cloudflare Access, but the direct public IP path could bypass that protection.

This meant the access model was inconsistent:

```text
Protected path:
User -> Cloudflare Access -> ui.testedcloud.com -> On-prem lab

Unprotected path:
User -> Public IP:8082 -> On-prem lab
```

### Root Cause

The router still had public port forwarding enabled to the on-prem lab service.

Cloudflare Access protected the domain-based path, but it could not protect direct public IP access if the router exposed the service separately.

### Resolution

The public router port forwarding rule to port `8082` was removed.

After the rule was removed, direct public IP access no longer worked.

The intended external access path became:

```text
User
    |
    v
Cloudflare Access
    |
    v
ui.testedcloud.com
    |
    v
On-prem lab
```

### Validation

Validation performed:

* Confirmed access through `https://ui.testedcloud.com`.
* Confirmed Cloudflare Access protected the domain.
* Confirmed direct public IP access no longer worked.
* Confirmed the lab UI remained reachable through the protected path.

### Lesson Learned

A protected domain does not automatically secure a service if another public path still exposes the same backend.

For protected access models, every external entry point must be reviewed.

### Portfolio Value

This issue demonstrates practical security troubleshooting:

* Identified a bypass path
* Removed unnecessary public exposure
* Preserved functionality through the protected path
* Improved the overall access model

## 4\. Issue 2 — Cloudflare Access Session Expiration Appeared as CORS Issue

### Problem

At one point, requests from the browser appeared to fail in a way that resembled a CORS issue.

The UI behavior suggested that the frontend could not reach the backend properly.

### Impact

This created confusion because the symptoms looked application-related, but the real problem was access/session-related.

Potential suspected causes included:

* CORS configuration
* Backend availability
* Reverse proxy behavior
* Cloudflare Access behavior
* Browser session state

### Root Cause

The Cloudflare Access session had expired.

Because the browser no longer had a valid Access session, requests did not behave as expected.

This made the issue appear similar to a CORS or browser-side request failure.

### Resolution

The user re-authenticated through Cloudflare Access.

After logging in again, the UI worked as expected.

### Validation

Validation performed:

* Re-authenticated with Cloudflare Access.
* Reloaded the protected UI.
* Confirmed the frontend and backend communication worked again.
* Confirmed no application code change was required.

### Lesson Learned

Access-layer failures can look like application-layer failures.

When using Cloudflare Access or any identity-aware proxy, troubleshooting should include:

* Browser session validity
* Access policy status
* Authentication cookies/tokens
* Backend availability
* CORS configuration
* Reverse proxy behavior

### Portfolio Value

This issue demonstrates practical troubleshooting across layers:

* Browser behavior
* Access control
* Reverse proxy
* Application communication
* Security session management

## 5\. Issue 3 — Cloud Run Used Default Compute Engine Service Account

### Problem

The Cloud Run service:

```text
testedcloud-consumer
```

was initially using the default Compute Engine service account:

```text
644725546932-compute@developer.gserviceaccount.com
```

### Impact

Using the default service account made the security model less clear and less production-like.

Potential risks:

* Overly broad permissions
* Higher blast radius
* Harder auditability
* Harder troubleshooting
* Poor separation of workload identities

### Root Cause

During the initial prototype phase, Cloud Run was deployed using the default runtime identity.

This is common during early builds, but it is not ideal for a portfolio or production-style architecture.

### Resolution

A dedicated service account was used for Cloud Run:

```text
testedcloud-consumer-sa@majestic-layout-255620.iam.gserviceaccount.com
```

Cloud Run was migrated to use this dedicated runtime identity.

### Validation

Validation performed:

* Confirmed Cloud Run uses `testedcloud-consumer-sa`.
* Sent valid events through the pipeline.
* Confirmed Cloud Run processed the events.
* Confirmed BigQuery received the processed records.
* Confirmed the pipeline still worked after the change.

### Lesson Learned

Runtime identity should map to workload responsibility.

A Cloud Run service that processes events and writes to BigQuery should use a dedicated identity with only the required permissions.

### Portfolio Value

This issue demonstrates:

* Cloud Run runtime identity awareness
* IAM least-privilege improvement
* Production-style service account design
* Validation after security changes

## 6\. Issue 4 — Default Compute Engine Service Account Had Broad Roles

### Problem

The default Compute Engine service account had broad permissions, including:

```text
roles/editor
roles/bigquery.dataEditor
```

### Impact

This increased the blast radius of the default service account.

If any workload using the default service account were compromised, it could potentially access or modify more resources than necessary.

### Root Cause

Broad roles were likely added or inherited during earlier lab/prototype work.

This is common in early experiments, but should be corrected before presenting the architecture as production-style.

### Resolution

The following roles were removed from the default Compute Engine service account:

```text
roles/editor
roles/bigquery.dataEditor
```

After cleanup, validation showed no remaining IAM bindings for:

```text
644725546932-compute@developer.gserviceaccount.com
```

### Validation

Validation performed:

* Checked IAM bindings.
* Confirmed no remaining IAM bindings for the default Compute Engine service account.
* Re-tested the pipeline.
* Confirmed events still reached BigQuery.
* Confirmed Cloud Run still processed messages.

### Lesson Learned

Removing broad IAM roles should always be followed by functional validation.

A successful hardening change is not only removing permissions; it is proving the system still works with the reduced permission set.

### Portfolio Value

This issue demonstrates:

* Least-privilege IAM hardening
* Blast-radius reduction
* IAM validation
* Cloud security maturity
* Operational discipline

## 7\. Issue 5 — DLQ Needed Validation With Malformed Payload

### Problem

The architecture included a Pub/Sub dead-letter queue, but it needed to be validated with an actual failed message.

A DLQ configuration is only useful if it is tested.

### Impact

Without validation, it would not be clear whether failed messages would actually reach the DLQ after retry exhaustion.

### Root Cause

The initial pipeline validated the happy path, but failure behavior still needed to be tested.

### Resolution

A malformed payload was intentionally published:

```json
{"bad\_payload": true}
```

Command used:

```bash
gcloud pubsub topics publish testedcloud-events \\
  --message='{"bad\_payload": true}'
```

The Pub/Sub subscription retried delivery and eventually routed the failed message to the DLQ after five attempts.

### Validation

The DLQ subscription was inspected with:

```bash
gcloud pubsub subscriptions pull testedcloud-events-dlq-sub \\
  --limit=10 \\
  --auto-ack
```

Validated result:

```text
Malformed payload reached the DLQ after 5 attempts.
```

### Lesson Learned

Event-driven systems should be tested under both successful and failure conditions.

A tested DLQ provides a controlled path for isolating and troubleshooting failed messages.

### Portfolio Value

This issue demonstrates:

* Pub/Sub retry behavior
* Dead-letter queue validation
* Failure-path testing
* Operational reliability thinking
* Troubleshooting maturity beyond happy-path demos

## 8\. Issue 6 — Missing `/api/health` Endpoint

### Problem

The API currently does not expose a dedicated health endpoint:

```text
/api/health
```

### Impact

Without a health endpoint, it is harder to implement:

* Uptime checks
* Load balancer health checks
* Synthetic monitoring
* Simple operational validation
* Readiness/liveness checks
* Dashboard health indicators

### Root Cause

The initial API focused on event publishing and pipeline validation. Operational health endpoints were not part of the first implementation.

### Current Status

Open improvement.

### Recommended Resolution

Add a lightweight health endpoint to the backend API.

Example expected response:

```json
{
  "status": "ok",
  "service": "testedcloud-api",
  "timestamp": "2026-05-05T00:00:00Z"
}
```

Potential future enhancements:

* Add Pub/Sub connectivity check
* Add BigQuery connectivity check
* Add version/build metadata
* Add environment metadata
* Add dependency status

### Validation Plan

After implementing `/api/health`, validate with:

```bash
curl http://localhost:8082/api/health
```

Expected result:

```text
HTTP 200
```

Expected JSON:

```json
{
  "status": "ok"
}
```

### Portfolio Value

Adding `/api/health` would demonstrate:

* Operational readiness
* Monitoring readiness
* Production-style API design
* Better support for uptime checks

## 9\. Issue 7 — Hardcoded Frontend API Key

### Problem

The frontend currently includes an API key in a hardcoded or client-exposed way.

### Impact

Any value embedded in frontend code should be treated as public because users can inspect browser JavaScript and network traffic.

Potential risks:

* Key exposure
* Unauthorized event submission
* Weak trust boundary
* Difficulty rotating secrets
* Poor production security posture

### Root Cause

The early lab prioritized functionality and quick end-to-end validation.

The API key approach was useful for a prototype, but it should be improved before broader exposure.

### Current Status

Open improvement.

### Recommended Resolution Options

Potential improvements:

1. Move sensitive validation to the backend only.
2. Use environment variables for server-side configuration.
3. Use Secret Manager for sensitive runtime configuration.
4. Use Cloudflare Access identity headers to identify authenticated users.
5. Implement stricter server-side validation.
6. Add rate limiting or abuse protection.
7. Avoid treating frontend secrets as real secrets.

### Suggested Target Model

A better model would be:

```text
Browser
    |
    v
Cloudflare Access
    |
    v
Protected UI/API
    |
    v
Backend validates request context
    |
    v
Backend publishes to Pub/Sub
```

The frontend should not be trusted as a secure place to store secrets.

### Portfolio Value

This issue is valuable to document because it shows awareness of application security trade-offs.

It demonstrates that the current lab has known limitations and a plan to improve them.

## 10\. Issue 8 — DNS and Domain Separation

### Problem

The project needed a clean domain model separating public portfolio content from protected operational lab access.

### Impact

Without clear domain separation, the architecture could become confusing:

* Public visitors may land on operational lab UI.
* Protected lab access could be mixed with public portfolio content.
* Future API exposure could become unclear.
* Recruiters or hiring managers may not know where to go.

### Root Cause

The domain evolved as the lab grew.

Initially, the focus was making the lab reachable. Later, the project needed a more intentional portfolio-facing domain structure.

### Target Resolution

Use the following separation:

|Domain|Purpose|Access|
|-|-|-|
|`testedcloud.com`|Public portfolio landing page|Public|
|`ui.testedcloud.com`|Protected lab UI|Cloudflare Access|
|`api.testedcloud.com`|Optional future API endpoint|Protected / restricted|

### Current Design Principle

The root domain should be used for portfolio storytelling.

The protected lab UI should remain behind Cloudflare Access.

The API domain should only be introduced when there is a clear access and security model.

### Portfolio Value

This improves the professional presentation of the project.

It makes the lab easier to explain:

```text
testedcloud.com = public story
ui.testedcloud.com = protected working lab
api.testedcloud.com = optional future API surface
```

## 11\. Cross-Layer Troubleshooting Lessons

The TestedCloud build required troubleshooting across multiple layers:

|Layer|Examples|
|-|-|
|Browser|Access session expiration, apparent CORS behavior|
|Edge access|Cloudflare Access protection|
|Home network|Router port forwarding exposure|
|Reverse proxy|NGINX path to UI/API|
|Application|Missing health endpoint, frontend key exposure|
|Pub/Sub|DLQ, push subscription, retry behavior|
|Cloud Run|Runtime service account, processing logs|
|BigQuery|Event insert validation|
|IAM|Default service account cleanup|
|VPC|Private VM and IAP SSH|
|DNS|Root domain and subdomain separation|

This is important because real hybrid cloud troubleshooting often requires moving between layers rather than focusing on only one service.

## 12\. Operational Maturity Improvements

Recommended next improvements based on the troubleshooting history:

1. Add `/api/health`.
2. Add structured logs to the API and Cloud Run consumer.
3. Add Cloud Monitoring alerts for Cloud Run 5xx errors.
4. Add Pub/Sub subscription backlog alerts.
5. Add DLQ message count alerts.
6. Add budget alerts.
7. Add API request validation.
8. Move sensitive runtime values to Secret Manager.
9. Add a clear DNS/access model to the public documentation.
10. Capture command outputs under `docs/evidence`.

## 13\. Suggested Evidence Files

Recommended evidence files:

```text
docs/evidence/public-port-forwarding-removed.txt
docs/evidence/cloudflare-access-validation.txt
docs/evidence/cloud-run-service-account-validation.txt
docs/evidence/default-compute-sa-cleanup.txt
docs/evidence/dlq-pull-output.txt
docs/evidence/bigquery-events-after-hardening.txt
docs/evidence/iap-ssh-validation.txt
```

## 14\. Interview Explanation

A concise way to explain the troubleshooting work:

> While building TestedCloud, I documented several real troubleshooting scenarios instead of only showing the final state. For example, I found that router port forwarding exposed the lab directly and bypassed Cloudflare Access, so I removed that path and validated protected access through the domain. I also fixed the Cloud Run identity model by moving away from the default compute service account, removed broad IAM roles, and validated that the pipeline still worked. Finally, I tested failure handling by publishing a malformed Pub/Sub message and confirming it reached the DLQ after retry exhaustion.

A more technical version:

> The troubleshooting work covered multiple layers: Cloudflare Access, browser session behavior, home router exposure, Docker/NGINX routing, Cloud Run runtime identity, Pub/Sub retry behavior, BigQuery validation, and IAM hardening. The most important pattern was validating every change after applying it, especially security changes, to ensure the system remained functional with a tighter architecture.

## 15\. Customer Engineer Relevance

This troubleshooting history is relevant to Customer Engineer and Cloud Architect roles because it demonstrates the ability to:

* Diagnose issues across layers
* Identify root cause instead of only symptoms
* Improve security posture after discovering a gap
* Validate changes after remediation
* Explain trade-offs and risks clearly
* Document operational lessons
* Think like a customer-facing technical advisor
* Connect technical troubleshooting to business and security outcomes

## 16\. Final Positioning

The TestedCloud troubleshooting log shows that the project is more than a static demo.

It documents the process of building, breaking, fixing, validating, and improving a hybrid cloud platform.

This strengthens the portfolio because it demonstrates practical engineering judgment, operational maturity, and the ability to communicate technical issues clearly.

