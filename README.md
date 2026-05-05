# TestedCloud — Hybrid Cloud Architecture Lab

TestedCloud is a hybrid cloud portfolio lab designed to demonstrate how on-prem infrastructure can securely integrate with Google Cloud services for event ingestion, serverless processing, analytics, observability, IAM hardening, and private networking.

The project connects an on-prem Ubuntu/Docker environment running on an Intel NUC with Google Cloud services including Pub/Sub, Cloud Run, BigQuery, Looker Studio, custom VPC networking, private VM access through IAP, and Cloudflare Access for protected external access.

## Purpose

This lab was built to translate industrial networking and secure infrastructure experience into practical cloud architecture patterns relevant to Google Cloud Customer Engineer and Cloud Architect roles.

The platform demonstrates:

- Hybrid on-prem to cloud event ingestion
- Serverless event processing with Cloud Run
- Pub/Sub push subscriptions with authenticated invocation
- BigQuery-based analytics
- Looker Studio dashboard integration
- IAM least-privilege hardening
- Custom VPC design with private subnets
- IAP-based SSH access to private VMs
- Cloudflare Access protection for external lab UI
- Dead-letter queue validation
- Troubleshooting and operational documentation

## Current Architecture

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
BigQuery: testedcloud_events.hybrid_events
    |
    v
Looker Studio / Dashboard Views
