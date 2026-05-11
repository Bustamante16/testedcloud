# TestedCloud Pending Issues

Last updated: May 11, 2026

## Priority 1 — Week 1 Stabilization

- [ ] Refresh Docker container validation
- [ ] Refresh NGINX reverse proxy validation
- [ ] Refresh FastAPI health endpoint validation
- [ ] Refresh Pub/Sub publishing validation
- [ ] Refresh Cloud Run consumer validation
- [ ] Refresh BigQuery insert validation
- [ ] Refresh IAM/security validation
- [ ] Refresh repository hygiene validation

## Priority 2 — TestedCloud Core

- [ ] Confirm all current architecture docs are still accurate
- [ ] Confirm event schema is documented
- [ ] Confirm Cloud Run consumer behavior is documented
- [ ] Confirm BigQuery table/view structure is documented
- [ ] Confirm Looker dashboard metrics are documented
- [ ] Confirm troubleshooting scenarios are documented

## Priority 3 — TestedCloud Chat

- [ ] Review current Firestore conversation model
- [ ] Identify old/new conversation mismatch
- [ ] Add `hiddenForUsers` field design
- [ ] Implement hide conversation only for requester
- [ ] Validate requester visibility logic
- [ ] Document Firestore rules
- [ ] Prepare analytics event integration

## Priority 4 — Security

- [ ] Confirm dedicated service accounts are still in use
- [ ] Confirm default Compute Engine service account is not overused
- [ ] Confirm Cloud Run invoker permissions
- [ ] Confirm Pub/Sub push authentication
- [ ] Confirm private VM has no public IP
- [ ] Confirm IAP SSH firewall rule
- [ ] Confirm Cloudflare Access rules

## Priority 5 — Future Modules

### TestedCloud Rack

- [ ] Define MVP scope
- [ ] Define rack node data model
- [ ] Define infrastructure event schema
- [ ] Define health monitoring endpoints
- [ ] Define BigQuery analytics fields

### TestedCloud AI

- [ ] Define MVP scope
- [ ] Define AI suggestion endpoint
- [ ] Define prompt architecture
- [ ] Define AI usage event schema
- [ ] Define latency metrics

### TestedCloud Monitor

- [ ] Define unified dashboard scope
- [ ] Define Core metrics
- [ ] Define Chat metrics
- [ ] Define Rack metrics
- [ ] Define AI metrics
