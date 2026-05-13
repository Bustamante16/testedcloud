# IAM and Security Validation — Week 1

Date: May 11, 2026
Scope: Service accounts, Cloud Run IAM, Pub/Sub push authentication, BigQuery IAM, private VM, and IAP SSH firewall

## Objective

Validate that TestedCloud follows a least-privilege-oriented security model using dedicated service accounts, authenticated Pub/Sub push delivery, private VM access, and controlled Cloud Run invocation.

## Service Accounts

Command:

    gcloud iam service-accounts list

Validated service accounts:

    testedcloud-consumer-sa@majestic-layout-255620.iam.gserviceaccount.com
    testedcloud-api-sa@majestic-layout-255620.iam.gserviceaccount.com
    pubsub-cloudrun-invoker@majestic-layout-255620.iam.gserviceaccount.com

Additional default service account observed:

    644725546932-compute@developer.gserviceaccount.com

Status:

    PASS

## Cloud Run Runtime Identity

Command:

    gcloud run services describe testedcloud-consumer \
      --region=us-central1 \
      --format="value(spec.template.spec.serviceAccountName)"

Result:

    testedcloud-consumer-sa@majestic-layout-255620.iam.gserviceaccount.com

Status:

    PASS

## Cloud Run Invocation Policy

Command:

    gcloud run services get-iam-policy testedcloud-consumer \
      --region=us-central1

Result:

    role: roles/run.invoker
    member: serviceAccount:pubsub-cloudrun-invoker@majestic-layout-255620.iam.gserviceaccount.com

No public invoker binding was found.

Not present:

    allUsers
    allAuthenticatedUsers

Status:

    PASS

## Pub/Sub Push Subscription Authentication

Command:

    gcloud pubsub subscriptions describe testedcloud-consumer-sub

Validated subscription:

    testedcloud-consumer-sub

Push endpoint:

    https://testedcloud-consumer-644725546932.us-central1.run.app/

OIDC service account:

    pubsub-cloudrun-invoker@majestic-layout-255620.iam.gserviceaccount.com

Retry policy:

    minimumBackoff: 10s
    maximumBackoff: 60s

Dead-letter policy:

    deadLetterTopic: projects/majestic-layout-255620/topics/testedcloud-events-dlq
    maxDeliveryAttempts: 5

Status:

    PASS

## Project IAM Review

Command:

    gcloud projects get-iam-policy majestic-layout-255620 \
      --flatten="bindings[].members" \
      --filter="bindings.members:serviceAccount" \
      --format="table(bindings.role, bindings.members)"

Validated custom service account roles:

    testedcloud-consumer-sa:
      roles/bigquery.dataEditor
      roles/bigquery.jobUser
      roles/pubsub.subscriber

    testedcloud-api-sa:
      roles/pubsub.publisher
      roles/bigquery.dataViewer
      roles/bigquery.jobUser

Finding:

    No custom TestedCloud service account was observed with roles/editor or roles/owner.

Observation:

    Some Google-managed service agents still have broad legacy roles such as roles/editor.
    These were not modified during this validation because they may be managed by Google Cloud services or legacy service integrations.

Status:

    PASS WITH OBSERVATION

## BigQuery Dataset IAM

Command:

    bq show --format=prettyjson majestic-layout-255620:testedcloud_events

Dataset:

    testedcloud_events

Observed dataset access:

    projectWriters: WRITER
    projectOwners: OWNER
    projectReaders: READER
    darbus13@gmail.com: OWNER

Finding:

    Dataset-level access currently relies on project-level groups and explicit owner access.
    Custom service account access is primarily controlled through project IAM roles.

Recommendation:

    For future hardening, evaluate dataset-level IAM bindings for more granular access control.

Status:

    PASS WITH IMPROVEMENT OPPORTUNITY

## Private VM Validation

Command:

    gcloud compute instances list

Validated instance:

    testedcloud-vm-app-test

Zone:

    us-central1-a

Machine type:

    e2-micro

Internal IP:

    10.10.1.2

External IP:

    None

Status:

    PASS

## IAP SSH Firewall Rule

Command:

    gcloud compute firewall-rules list \
      --filter="name~iap OR sourceRanges:35.235.240.0/20"

Validated firewall rule:

    testedcloud-allow-iap-ssh

Network:

    testedcloud-vpc

Direction:

    INGRESS

Allowed:

    tcp:22

Status:

    PASS

## Cloud Run Public Access Check

Command:

    gcloud run services get-iam-policy testedcloud-consumer \
      --region=us-central1 \
      --format="json"

Result:

    Only pubsub-cloudrun-invoker has roles/run.invoker.
    No allUsers or allAuthenticatedUsers binding was found.

Status:

    PASS

## Final Result

IAM and security validation result:

    PASS

TestedCloud currently uses a strong security posture for the lab environment:

- Dedicated service accounts are used for API publishing, Cloud Run consumption, and Pub/Sub invocation.
- Cloud Run is not publicly invokable.
- Pub/Sub push delivery uses OIDC.
- The private VM has no external IP.
- IAP SSH firewall access is present.
- No custom TestedCloud service account currently has broad Editor or Owner permissions.

## Follow-Up Improvements

- Review whether dataset-level IAM should be made more granular.
- Keep monitoring Google-managed service agents with broad legacy roles.
- Move hardcoded local API key from docker-compose.yml into an untracked .env file in a future cleanup task.
