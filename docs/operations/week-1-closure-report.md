# Week 1 Closure Report — TestedCloud Stabilization

Date: May 13, 2026  
Week: May 11 – May 17, 2026  
Focus: Infrastructure stabilization, validation evidence, repository hygiene, and portfolio readiness

## Objective

The objective of Week 1 was to stabilize and validate the existing TestedCloud foundation before adding new features or starting additional modules such as TestedCloud Rack, TestedCloud AI, or TestedCloud Monitor.

## Weekly Rule

No new features were added during this stabilization week.

Focus areas:

- Validate existing infrastructure
- Confirm event pipeline functionality
- Review IAM and security posture
- Review repository hygiene
- Update documentation and evidence
- Prepare the next week of work

## Completed Work

### Day 1 — Repository and Documentation Inventory

Status:

    COMPLETED

Completed items:

- Confirmed repository path: `~/testedcloud-lab`
- Confirmed branch: `main`
- Confirmed GitHub sync
- Reviewed existing documentation structure
- Created Week 1 stabilization tracking documents
- Created current status and pending issues documents

### Day 2 — On-Prem Validation

Status:

    COMPLETED

Validated items:

- Docker engine
- Docker Compose services
- TestedCloud API container
- TestedCloud NGINX reverse proxy
- TestedCloud UI container
- Correct local route: `http://localhost:8082`
- FastAPI health endpoint through NGINX
- Event publishing through NGINX to FastAPI and Pub/Sub

Key finding:

- `localhost:8080` belongs to WordPress, not TestedCloud API.
- `localhost:80` belongs to the general nginx-proxy / WordPress route.
- `localhost:8082` is the correct TestedCloud local route.

Evidence:

- `docs/evidence/onprem-validation-week-1.md`

### Day 3 — GCP Event Pipeline Validation

Status:

    COMPLETED

Validated flow:

    On-prem API
    -> Pub/Sub
    -> Cloud Run consumer
    -> BigQuery
    -> BigQuery views

Validated event:

    d7ee44d5-8221-4fa0-a1ca-17179c14cb11

Validated items:

- Active GCP project
- Pub/Sub topics
- Pub/Sub subscriptions
- Cloud Run consumer
- Pub/Sub push subscription
- OIDC invoker service account
- BigQuery insert
- Latency calculation
- Dashboard and latency views

Evidence:

- `docs/evidence/gcp-pipeline-validation-week-1.md`

### Day 4 — IAM and Security Validation

Status:

    COMPLETED

Validated items:

- Dedicated service accounts
- Cloud Run runtime identity
- Cloud Run invocation policy
- Pub/Sub OIDC push authentication
- Project IAM roles for custom service accounts
- BigQuery IAM observations
- Private VM without public IP
- IAP SSH firewall rule
- Cloudflare Access protection

Key findings:

- Cloud Run is not publicly invokable.
- Pub/Sub invokes Cloud Run using `pubsub-cloudrun-invoker`.
- `testedcloud-consumer-sa` is used as Cloud Run runtime identity.
- Private VM has no external IP.
- `ui.testedcloud.com` redirects unauthenticated users to Cloudflare Access login.
- `testedcloud.com/api/health` currently routes to GitHub Pages, not the on-prem API.

Evidence:

- `docs/evidence/iam-security-validation-week-1.md`
- `docs/evidence/cloudflare-access-week-1.md`

### Day 5 — Repository Hygiene

Status:

    COMPLETED

Validated items:

- `.gitignore`
- Local runtime config exclusion
- Sensitive file search
- Hardcoded secret keyword search
- Tracked sensitive files check
- Docker Compose tracking check
- Frontend config tracking check
- Firebase/Android config review

Key findings:

- `docker-compose.yml` exists locally but is not tracked by Git.
- `frontend/config.js` exists locally but is not tracked by Git.
- No obvious private keys, service account JSON credentials, `.env`, or credential files are tracked by Git.
- Android password references are application variables, not hardcoded real passwords.

Evidence:

- `docs/evidence/repository-hygiene-week-1.md`

### Day 6 — README and Portfolio Review

Status:

    COMPLETED

Validated items:

- README project purpose
- Platform module descriptions
- Portfolio narrative
- Architecture overview
- Core components
- Security highlights
- Validated outcomes
- Observed metrics
- Documentation index
- Evidence links
- Interview positioning

Updates applied:

- Aligned naming from `TestedChat` to `TestedCloud Chat`
- Added Week 1 stabilization evidence references
- Added note that observed metrics are validation samples

Evidence:

- `docs/evidence/readme-portfolio-review-week-1.md`

## Final Week 1 Outcome

Week 1 stabilization result:

    PASS

TestedCloud Core is currently validated across:

- On-prem Docker services
- NGINX reverse proxy
- FastAPI backend
- Pub/Sub publishing
- Cloud Run consumer processing
- BigQuery ingestion
- BigQuery latency/dashboard views
- IAM/service account separation
- Cloud Run private invocation
- Pub/Sub OIDC authentication
- Private VM access model
- IAP SSH firewall
- Cloudflare Access protection
- Repository hygiene

## Main Strengths Confirmed

- The hybrid event pipeline is operational end to end.
- The on-prem route and public protected route are now clearly understood.
- IAM is separated by workload responsibility.
- Cloud Run is not publicly invokable.
- Evidence is now organized and easier to defend in a portfolio or interview.
- The repository does not currently track obvious secrets or private credentials.

## Main Follow-Up Items

- Continue keeping local runtime config untracked.
- Consider replacing local shared API key with a stronger auth pattern.
- Consider Secret Manager for production-style runtime configuration.
- Review dataset-level IAM granularity in BigQuery.
- Continue improving documentation links and evidence index.
- Start Week 2 with TestedCloud Chat stabilization.

## Week 2 Recommended Focus

Week 2 should focus on TestedCloud Chat Core.

Recommended priorities:

1. Review Firestore conversation data model
2. Identify old/new conversation mismatch
3. Design `hiddenForUsers`
4. Implement hide conversation only for requester
5. Validate requester visibility logic
6. Review Firestore rules
7. Prepare Chat analytics events
8. Capture screenshots and evidence
9. Push stable commit

## Final Statement

Week 1 successfully stabilized the TestedCloud foundation and created evidence that the platform is operational, secured, documented, and ready for the next phase of development.
