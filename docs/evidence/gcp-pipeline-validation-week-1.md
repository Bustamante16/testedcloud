# GCP Pipeline Validation — Week 1

Date: May 11, 2026
Scope: Pub/Sub, Cloud Run consumer, BigQuery table, BigQuery views, and latency validation

## Objective

Validate that events published from the on-premises TestedCloud environment are delivered to Pub/Sub, pushed to Cloud Run, processed by the consumer, inserted into BigQuery, and exposed through dashboard/latency views.

## Validated Event

Event ID:

    d7ee44d5-8221-4fa0-a1ca-17179c14cb11

Message ID:

    19022738486043056

Event type:

    week_1_onprem_validation

Source:

    testedcloud-core

Origin:

    on-prem-nuc

## Active GCP Project

Command:

    gcloud config get-value project

Result:

    majestic-layout-255620

Status:

    PASS

## Pub/Sub Topics

Command:

    gcloud pubsub topics list

Validated topics:

    projects/majestic-layout-255620/topics/testedcloud-events
    projects/majestic-layout-255620/topics/testedcloud-events-dlq

Status:

    PASS

## Pub/Sub Subscriptions

Command:

    gcloud pubsub subscriptions list

Validated subscriptions:

    testedcloud-events-sub
    testedcloud-consumer-sub
    testedcloud-events-dlq-sub

The primary push subscription is:

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

## Cloud Run Consumer

Command:

    gcloud run services list --region=us-central1

Validated service:

    testedcloud-consumer

Region:

    us-central1

URL:

    https://testedcloud-consumer-644725546932.us-central1.run.app

Status:

    PASS

## Cloud Run Processing Evidence

Command:

    gcloud run services logs read testedcloud-consumer --region=us-central1 --limit=50

Relevant log:

    PIPELINE_EVENT_PROCESSED={
      "event_id": "d7ee44d5-8221-4fa0-a1ca-17179c14cb11",
      "source": "testedcloud-core",
      "event_type": "week_1_onprem_validation",
      "origin": "on-prem-nuc",
      "received_at": "2026-05-11T17:19:37.520236",
      "processed_at": "2026-05-11T17:19:43.394165+00:00",
      "target": "bigquery",
      "table": "majestic-layout-255620.testedcloud_events.hybrid_events"
    }

Status:

    PASS

## BigQuery Table Schema

Command:

    bq show --schema --format=prettyjson majestic-layout-255620:testedcloud_events.hybrid_events

Validated fields:

    event_id STRING
    received_at TIMESTAMP
    source STRING
    event_type STRING
    message STRING
    origin STRING
    processed_at TIMESTAMP
    user_email STRING

Finding:

    processing_delay_seconds is not a physical column in the hybrid_events table.
    Latency is calculated in queries or exposed through dashboard/latency views.

Status:

    PASS

## BigQuery Dataset Objects

Command:

    bq ls majestic-layout-255620:testedcloud_events

Validated objects:

    hybrid_events TABLE
    v_dashboard_events VIEW
    v_dashboard_events_v2 VIEW
    v_latency_metrics VIEW
    v_latency_metrics_v2 VIEW

Status:

    PASS

## BigQuery Event Row Validation

Command:

    bq query --use_legacy_sql=false '
    SELECT
      event_id,
      source,
      event_type,
      message,
      origin,
      received_at,
      processed_at,
      user_email
    FROM `majestic-layout-255620.testedcloud_events.hybrid_events`
    WHERE event_id = "d7ee44d5-8221-4fa0-a1ca-17179c14cb11"
    LIMIT 10
    '

Result summary:

    event_id: d7ee44d5-8221-4fa0-a1ca-17179c14cb11
    source: testedcloud-core
    event_type: week_1_onprem_validation
    message: Testing on-prem NGINX to FastAPI route
    origin: on-prem-nuc
    received_at: 2026-05-11 17:19:37
    processed_at: 2026-05-11 17:19:43
    user_email: unknown

Status:

    PASS

## BigQuery Latency Calculation

Command:

    bq query --use_legacy_sql=false '
    SELECT
      event_id,
      source,
      event_type,
      origin,
      received_at,
      processed_at,
      TIMESTAMP_DIFF(processed_at, received_at, SECOND) AS processing_delay_seconds
    FROM `majestic-layout-255620.testedcloud_events.hybrid_events`
    WHERE event_id = "d7ee44d5-8221-4fa0-a1ca-17179c14cb11"
    LIMIT 10
    '

Result summary:

    event_id: d7ee44d5-8221-4fa0-a1ca-17179c14cb11
    processing_delay_seconds: 5

Status:

    PASS

## Latency Metrics View

Command:

    bq query --use_legacy_sql=false '
    SELECT *
    FROM `majestic-layout-255620.testedcloud_events.v_latency_metrics`
    LIMIT 10
    '

Result:

    p50_latency: 0
    p95_latency: 5
    p99_latency: 5

Status:

    PASS

## Dashboard Events View

Command:

    bq query --use_legacy_sql=false '
    SELECT *
    FROM `majestic-layout-255620.testedcloud_events.v_dashboard_events_v2`
    ORDER BY processed_at DESC
    LIMIT 10
    '

Relevant result:

    event_id: d7ee44d5-8221-4fa0-a1ca-17179c14cb11
    source: testedcloud-core
    event_type: week_1_onprem_validation
    origin: on-prem-nuc
    user_email: unknown
    received_at: 2026-05-11 17:19:37
    processed_at: 2026-05-11 17:19:43
    latency_ms: 5873
    latency_seconds: 5.873
    event_date: 2026-05-11

Status:

    PASS

## Final Result

GCP pipeline validation result:

    PASS

The TestedCloud event pipeline is operational from the on-premises API to Pub/Sub, Cloud Run, BigQuery, and analytics views.
