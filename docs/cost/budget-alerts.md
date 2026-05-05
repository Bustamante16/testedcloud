# TestedCloud Budget Alerts

## 1\. Purpose

This document describes the budget alert strategy for the TestedCloud lab.

The goal is to prevent unexpected Google Cloud spend while continuing to build a realistic hybrid cloud portfolio environment.

Budget alerts are important because TestedCloud includes services that are low-cost at lab scale but can still generate charges if usage grows, resources are left running, logs increase, or future services such as Vertex AI are added.

## 2\. Billing Project Status

The TestedCloud Google Cloud project has billing enabled.

Project:

```text
majestic-layout-255620
```

Billing evidence is stored in:

```text
docs/evidence/billing-project-link.txt
```

The billing account ID is intentionally sanitized in the evidence file:

```text
billingAccountName: billingAccounts/XXXXXX-XXXXXX-XXXXXX
```

This allows the portfolio to show that billing is enabled without exposing the real billing account ID.

## 3\. Budget Alert Strategy

Recommended budget name:

```text
TestedCloud Lab Budget
```

Recommended alert thresholds:

|Threshold|Purpose|
|-|-|
|50%|Early awareness|
|80%|Action threshold|
|100%|Stop and review|
|120%|Optional escalation threshold|

The 50%, 80%, and 100% thresholds are the core recommended alerts.

The optional 120% threshold can be used as an escalation signal if the lab goes beyond the expected monthly budget.

## 4\. Recommended Monthly Budget

Because this is a personal portfolio lab, the initial monthly budget should be intentionally small.

Recommended starting point:

```text
$10 to $25 per month
```

Suggested option:

```text
$20 per month
```

This is not a hard spending limit. It is an alerting mechanism to notify when usage is higher than expected.

## 5\. Why Budget Alerts Matter

Budget alerts help detect:

* A private VM left running unnecessarily
* Unexpected Cloud Run invocations
* Increased BigQuery query usage
* Excessive Cloud Logging volume
* Pub/Sub retry loops
* Future Vertex AI experiments generating cost
* Forgotten resources after testing
* Unexpected dashboard refresh/query behavior
* New resources created during experiments and not cleaned up

## 6\. Main Cost Risks

|Risk|Mitigation|
|-|-|
|Compute Engine VM left running|Stop VM when not actively testing|
|BigQuery dashboard queries growing|Use efficient views and limited refreshes|
|Excessive Cloud Run logs|Avoid unnecessary logging|
|Pub/Sub retries from bad messages|Use DLQ and alerts|
|Vertex AI experiments|Configure alerts before enabling|
|Forgotten test resources|Use labels and monthly cleanup|
|Container image storage growth|Delete unused images periodically|
|Monitoring/logging growth|Review log volume and retention|

## 7\. Console Configuration Steps

Budget alerts can be configured from the Google Cloud Console.

Recommended steps:

1. Open Google Cloud Console.
2. Go to Billing.
3. Select the active billing account.
4. Go to Budgets \& alerts.
5. Create a new budget.
6. Scope the budget to project `majestic-layout-255620`.
7. Name it `TestedCloud Lab Budget`.
8. Set the monthly budget amount, for example `$20`.
9. Configure alert thresholds:

   * 50%
   * 80%
   * 100%
   * Optional: 120%
10. Confirm email notifications are enabled.
11. Save the budget.

## 8\. Recommended Budget Scope

Recommended scope:

|Setting|Recommended Value|
|-|-|
|Budget name|`TestedCloud Lab Budget`|
|Project scope|`majestic-layout-255620`|
|Monthly amount|`$20`|
|Alert 1|`50%`|
|Alert 2|`80%`|
|Alert 3|`100%`|
|Optional alert|`120%`|
|Notifications|Email enabled|

The budget should be scoped to the TestedCloud project, not necessarily the entire billing account, unless the billing account is only used for this lab.

## 9\. Evidence to Capture

After creating the budget alert, capture sanitized evidence in:

```text
docs/evidence/budget-alerts-config.txt
```

Suggested evidence content:

```text
Budget name: TestedCloud Lab Budget
Project: majestic-layout-255620
Billing account: XXXXXX-XXXXXX-XXXXXX
Amount: $20/month
Thresholds: 50%, 80%, 100%
Notifications: Email enabled
```

Do not commit the real billing account ID.

## 10\. Suggested Evidence Command

If budget data is retrieved using `gcloud`, sanitize it before committing.

Command pattern:

```bash
gcloud billing budgets list \\
  --billing-account=REAL\_BILLING\_ACCOUNT\_ID \\
  > docs/evidence/budget-alerts-config.txt
```

Sanitize the billing account ID:

```bash
sed -i 's/REAL\_BILLING\_ACCOUNT\_ID/XXXXXX-XXXXXX-XXXXXX/g' docs/evidence/budget-alerts-config.txt
```

Before committing, verify that the real billing account ID is not present:

```bash
grep -n "REAL\_BILLING\_ACCOUNT\_ID" docs/evidence/budget-alerts-config.txt || true
```

Expected result:

```text
No output
```

## 11\. Manual Evidence Option

If the budget is created through the Google Cloud Console and the `gcloud` command is not used, create a manual sanitized evidence file.

Example:

```bash
cat > docs/evidence/budget-alerts-config.txt <<'EOF'
Budget name: TestedCloud Lab Budget
Project: majestic-layout-255620
Billing account: XXXXXX-XXXXXX-XXXXXX
Amount: $20/month
Thresholds: 50%, 80%, 100%
Notifications: Email enabled
Created from: Google Cloud Console
EOF
```

Then verify:

```bash
cat docs/evidence/budget-alerts-config.txt
```

## 12\. Recommended Follow-Up Actions

After budget alerts are configured:

1. Add the sanitized budget evidence file.
2. Commit the evidence.
3. Add monitoring alerts for DLQ messages.
4. Add monitoring alerts for Cloud Run 5xx errors.
5. Add Pub/Sub backlog alerts.
6. Review VM runtime and stop it when unused.
7. Add labels to lab resources.
8. Review BigQuery dashboard refresh behavior.
9. Avoid Vertex AI experiments until budget alerts are active.
10. Add budget alert status to the main README or portfolio page.

## 13\. Related Evidence Files

Related evidence files:

|Evidence File|Purpose|
|-|-|
|`docs/evidence/billing-project-link.txt`|Shows billing is enabled for the project using a sanitized billing account ID|
|`docs/evidence/budget-alerts-config.txt`|Shows budget alert configuration|
|`docs/evidence/private-vm-config.txt`|Supports VM cost awareness|
|`docs/evidence/bigquery-latency-metrics.txt`|Supports analytics and dashboard validation|
|`docs/evidence/pubsub-subscription-dlq-config.txt`|Supports reliability and retry/DLQ behavior|

## 14\. Cost Governance Value

Budget alerts are part of basic cloud cost governance.

They help establish:

* Spend visibility
* Early warning signals
* Accountability
* Lab discipline
* Safer experimentation
* Readiness for future AI/ML experiments
* A stronger production-style portfolio story

Budget alerts do not replace cleanup, monitoring, or architecture review. They provide a financial signal when usage exceeds expectations.

## 15\. Interview Explanation

A concise way to explain this in an interview:

> I documented cost considerations for TestedCloud and configured a budget alert strategy because even small labs can generate unexpected spend. The architecture is intentionally lightweight, using an on-prem NUC, Pub/Sub, Cloud Run, BigQuery, and a small private VM. I planned alerts at 50%, 80%, and 100% of a small monthly budget so I can detect unusual usage early, especially before adding higher-cost services like Vertex AI.

A more technical version:

> The cost model separates fixed local infrastructure from variable cloud services. The main cost drivers are Cloud Run invocations, Pub/Sub message volume, BigQuery storage/query usage, logging volume, and running Compute Engine instances. Budget alerts provide a governance layer so I can catch unexpected usage before expanding into higher-cost services such as Vertex AI.

## 16\. Customer Engineer Relevance

This budget alert strategy is relevant to Customer Engineer and Cloud Architect roles because it demonstrates the ability to:

* Discuss cost governance clearly
* Connect cloud architecture to financial controls
* Recommend budget alerts before scaling experiments
* Identify likely cost drivers
* Explain serverless and managed-service cost trade-offs
* Prepare a lab or proof of concept for safer growth
* Balance technical implementation with business awareness

## 17\. Final Positioning

Budget alerts show that TestedCloud is not only technically functional, but also operated with financial awareness.

This strengthens the portfolio by demonstrating practical cloud governance, cost control, and production-style discipline.

