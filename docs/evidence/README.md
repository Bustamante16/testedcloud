# TestedCloud Evidence Index

## 1\. Purpose

This folder stores command outputs, validation results, logs, and screenshots that support the TestedCloud portfolio documentation.

The purpose of this evidence folder is to make the project verifiable. Instead of only describing the architecture, this folder captures proof that the platform was built, tested, hardened, and validated.

This evidence index supports the main TestedCloud portfolio narrative:

> TestedCloud demonstrates how on-prem workloads can securely integrate with Google Cloud services for event ingestion, serverless processing, analytics, observability, IAM hardening, private networking, and operational troubleshooting.

## 2\. Evidence Strategy

Each evidence file should support one or more of the following areas:

* Core platform validation
* IAM hardening
* Pub/Sub and DLQ behavior
* Cloud Run processing
* BigQuery inserts and views
* VPC and private VM configuration
* IAP SSH access
* Cloudflare Access protection
* Cost and budget controls
* Monitoring and alerting
* Troubleshooting outcomes

The evidence should prove that the architecture was not only designed, but actually implemented and validated.

## 3\. Recommended Evidence Files

|File|Purpose|
|-|-|
|`cloud-run-service-identity.txt`|Shows Cloud Run running as `testedcloud-consumer-sa`|
|`pubsub-push-oidc-config.txt`|Shows Pub/Sub push subscription using OIDC|
|`pubsub-subscription-dlq-config.txt`|Shows Pub/Sub DLQ configuration|
|`iam-policy-after-hardening.txt`|Shows default Compute Engine service account cleanup|
|`pipeline-validation-after-iam-hardening.txt`|Shows pipeline still works after IAM changes|
|`dlq-test-publish-output.txt`|Shows malformed payload was published|
|`dlq-pull-output.txt`|Shows malformed payload reached DLQ|
|`cloud-run-logs-dlq-validation.txt`|Shows logs related to DLQ validation|
|`cloud-run-logs-recent.txt`|Shows recent Cloud Run processing logs|
|`bigquery-events-sample.txt`|Shows events stored in BigQuery|
|`bigquery-latency-metrics.txt`|Shows latency metrics from BigQuery views|
|`vpc-subnet-app.txt`|Shows app subnet configuration|
|`vpc-subnet-data.txt`|Shows data subnet configuration|
|`private-vm-config.txt`|Shows private VM configuration|
|`iap-firewall-rule.txt`|Shows IAP SSH firewall rule configuration|
|`budget-alerts-config.txt`|Shows budget alert configuration when created|
|`monitoring-alerts-config.txt`|Shows monitoring alert policies when created|

## 4\. Evidence Collection Commands

### 4.1 Cloud Run Runtime Service Account

Command:

```bash
gcloud run services describe testedcloud-consumer \\
  --region=us-central1 \\
  --format="value(spec.template.spec.serviceAccountName)" \\
  > docs/evidence/cloud-run-service-identity.txt
```

Expected result:

```text
testedcloud-consumer-sa@majestic-layout-255620.iam.gserviceaccount.com
```

Purpose:

* Confirms that Cloud Run no longer uses the default Compute Engine service account.
* Confirms that the Cloud Run consumer uses the dedicated runtime service account.

### 4.2 Pub/Sub Push Subscription OIDC Configuration

Command:

```bash
gcloud pubsub subscriptions describe testedcloud-consumer-sub \\
  > docs/evidence/pubsub-push-oidc-config.txt
```

Important fields to verify:

```text
pushConfig:
  oidcToken:
    serviceAccountEmail: pubsub-cloudrun-invoker@majestic-layout-255620.iam.gserviceaccount.com
  pushEndpoint: https://testedcloud-consumer-644725546932.us-central1.run.app/
```

Purpose:

* Confirms that Pub/Sub invokes Cloud Run using OIDC authentication.
* Confirms the correct invoker service account is configured.
* Confirms the push endpoint points to the expected Cloud Run service.

### 4.3 Default Compute Engine Service Account Cleanup

Command:

```bash
gcloud projects get-iam-policy majestic-layout-255620 \\
  --flatten="bindings\[].members" \\
  --filter="bindings.members:644725546932-compute@developer.gserviceaccount.com" \\
  --format="table(bindings.role)" \\
  > docs/evidence/iam-policy-after-hardening.txt
```

Expected result:

```text
Listed 0 items.
```

Purpose:

* Confirms that the default Compute Engine service account no longer has broad project-level IAM bindings.
* Supports the IAM hardening documentation.

### 4.4 DLQ Subscription Configuration

Command:

```bash
gcloud pubsub subscriptions describe testedcloud-consumer-sub \\
  > docs/evidence/pubsub-subscription-dlq-config.txt
```

Important fields to verify:

```text
deadLetterPolicy:
  deadLetterTopic: projects/majestic-layout-255620/topics/testedcloud-events-dlq
  maxDeliveryAttempts: 5
```

Purpose:

* Confirms that the main Pub/Sub subscription has a DLQ configured.
* Confirms that the maximum delivery attempts are set to 5.

### 4.5 Publish Bad Payload for DLQ Test

Command:

```bash
gcloud pubsub topics publish testedcloud-events \\
  --message='{"bad\_payload": true}' \\
  > docs/evidence/dlq-test-publish-output.txt
```

Purpose:

* Captures evidence that a malformed payload was published for failure-path validation.
* Supports the DLQ validation document.

### 4.6 Pull Message From DLQ

Command:

```bash
gcloud pubsub subscriptions pull testedcloud-events-dlq-sub \\
  --limit=10 \\
  --auto-ack \\
  > docs/evidence/dlq-pull-output.txt
```

Purpose:

* Confirms that the malformed payload reached the DLQ.
* Supports the failure-handling validation.

### 4.7 Cloud Run Recent Logs

Command:

```bash
gcloud logging read \\
  'resource.type="cloud\_run\_revision" AND resource.labels.service\_name="testedcloud-consumer"' \\
  --limit=50 \\
  --format="table(timestamp,severity,textPayload)" \\
  > docs/evidence/cloud-run-logs-recent.txt
```

Expected signals:

```text
POST 204
PIPELINE\_EVENT\_PROCESSED
```

Purpose:

* Confirms recent Cloud Run processing.
* Supports the end-to-end pipeline validation.

### 4.8 BigQuery Event Sample

Command:

```bash
bq query --use\_legacy\_sql=false \\
'SELECT event\_id, received\_at, source, event\_type, origin, processed\_at, user\_email
 FROM `majestic-layout-255620.testedcloud\_events.hybrid\_events`
 ORDER BY processed\_at DESC
 LIMIT 10' \\
  > docs/evidence/bigquery-events-sample.txt
```

Purpose:

* Confirms processed events exist in BigQuery.
* Supports the analytics pipeline documentation.

### 4.9 BigQuery Latency Metrics

Command:

```bash
bq query --use\_legacy\_sql=false \\
'SELECT \*
 FROM `majestic-layout-255620.testedcloud\_events.v\_latency\_metrics\_v2`
 LIMIT 20' \\
  > docs/evidence/bigquery-latency-metrics.txt
```

Purpose:

* Captures latency metrics from the BigQuery dashboard view.
* Supports performance and dashboard documentation.

### 4.10 VPC App Subnet Configuration

Command:

```bash
gcloud compute networks subnets describe testedcloud-subnet-app \\
  --region=us-central1 \\
  > docs/evidence/vpc-subnet-app.txt
```

Purpose:

* Confirms the app subnet exists.
* Captures subnet configuration, CIDR, region, and Private Google Access status.

### 4.11 VPC Data Subnet Configuration

Command:

```bash
gcloud compute networks subnets describe testedcloud-subnet-data \\
  --region=us-central1 \\
  > docs/evidence/vpc-subnet-data.txt
```

Purpose:

* Confirms the data subnet exists.
* Captures subnet configuration, CIDR, region, and Private Google Access status.

### 4.12 Private VM Configuration

Command:

```bash
gcloud compute instances describe testedcloud-vm-app-test \\
  --zone=us-central1-a \\
  --format="yaml(name,zone,machineType,networkInterfaces)" \\
  > docs/evidence/private-vm-config.txt
```

Purpose:

* Confirms the private VM exists.
* Confirms the VM network configuration.
* Confirms whether the VM has no external IP.

### 4.13 IAP Firewall Rule

Command:

```bash
gcloud compute firewall-rules describe testedcloud-allow-iap-ssh \\
  > docs/evidence/iap-firewall-rule.txt
```

Expected fields:

```text
sourceRanges:
- 35.235.240.0/20
allowed:
- IPProtocol: tcp
  ports:
  - '22'
```

Purpose:

* Confirms IAP SSH firewall access is configured.
* Supports the private administration model.

## 5\. Evidence Quality Guidelines

Good evidence should be:

* Specific
* Reproducible
* Timestamped when possible
* Safe to publish
* Free of secrets, tokens, private keys, and sensitive personal data
* Linked to a documented architecture decision or validation test

Before committing evidence files, always inspect them:

```bash
cat docs/evidence/<file-name>
```

Or:

```bash
less docs/evidence/<file-name>
```

Do not commit files that contain:

* API keys
* OAuth tokens
* Service account private keys
* Access tokens
* Refresh tokens
* Session cookies
* Private keys
* Personal information
* Internal-only confidential information
* Public IPs if you prefer not to expose them

## 6\. Recommended Evidence Review Commands

Search for common sensitive patterns before committing:

```bash
grep -RniE "token|secret|password|private\_key|client\_secret|api\_key|authorization|bearer" docs/evidence || true
```

Check Git status:

```bash
git status
```

Review exact staged files:

```bash
git diff --cached --name-only
```

Review staged content:

```bash
git diff --cached
```

## 7\. Recommended Commit Pattern

Use small commits grouped by evidence type.

Example 1:

```bash
git add docs/evidence/cloud-run-service-identity.txt
git commit -m "Add Cloud Run service identity evidence"
```

Example 2:

```bash
git add docs/evidence/pubsub-push-oidc-config.txt docs/evidence/pubsub-subscription-dlq-config.txt
git commit -m "Add Pub/Sub subscription configuration evidence"
```

Example 3:

```bash
git add docs/evidence/bigquery-events-sample.txt docs/evidence/bigquery-latency-metrics.txt
git commit -m "Add BigQuery validation evidence"
```

Example 4:

```bash
git add docs/evidence/private-vm-config.txt docs/evidence/iap-firewall-rule.txt
git commit -m "Add private networking validation evidence"
```

## 8\. Suggested Evidence Collection Order

Recommended order:

1. Cloud Run service identity
2. Pub/Sub OIDC push configuration
3. IAM cleanup validation
4. DLQ configuration
5. BigQuery event sample
6. BigQuery latency metrics
7. Private VM configuration
8. IAP firewall rule
9. Recent Cloud Run logs
10. Budget and monitoring alerts after they are configured

This order prioritizes the evidence that best supports the current documentation.

## 9\. Public Portfolio Considerations

Some evidence files may be safe for a private repo but not ideal for a public portfolio.

Before publishing publicly, review whether the evidence exposes:

* Project IDs
* Service account emails
* Internal IP ranges
* Domain names
* Public IPs
* User emails
* Log contents
* Request payloads
* Any operational details you do not want public

Recommended public approach:

* Keep raw evidence in a private repo if needed.
* Publish sanitized snippets in the public portfolio.
* Use screenshots or redacted command outputs when appropriate.
* Avoid publishing secrets or sensitive operational metadata.

## 10\. Portfolio Value

This evidence folder makes TestedCloud stronger as a portfolio project because it proves that:

* The architecture was implemented, not only designed.
* IAM hardening was validated.
* The pipeline works end-to-end.
* Failure handling was tested.
* Private networking was configured.
* Operational troubleshooting was documented.
* The project follows a production-style documentation approach.

## 11\. Interview Explanation

A concise way to explain this folder in an interview:

> I created an evidence folder for TestedCloud to capture command outputs and validation artifacts. This way, the project is not only documented conceptually; it includes proof that Cloud Run uses the correct service account, Pub/Sub is configured with OIDC and DLQ behavior, the default compute service account was cleaned up, BigQuery receives events, and the private networking/IAP model is configured correctly.

A more technical version:

> For each architecture decision, I tried to capture supporting evidence. For example, IAM hardening is supported by Cloud Run service identity output and IAM policy checks. The DLQ design is supported by Pub/Sub subscription output and DLQ pull results. The analytics pipeline is supported by BigQuery query outputs. This makes the lab easier to review and defend in a technical interview.

## 12\. Final Positioning

The evidence folder supports the main TestedCloud narrative:

> TestedCloud is a practical hybrid cloud lab that demonstrates on-prem integration with Google Cloud using event ingestion, serverless processing, analytics, IAM hardening, private networking, operational validation, and documented troubleshooting.

The evidence folder helps prove that the system was built, tested, hardened, and validated with real command outputs.

