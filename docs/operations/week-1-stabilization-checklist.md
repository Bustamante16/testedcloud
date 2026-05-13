# Week 1 Stabilization Checklist

Date range: May 11 – May 17, 2026  
Focus: TestedCloud Core stabilization  
Rule: No new features this week

## Goal

Validate the current TestedCloud foundation before continuing with TestedCloud Chat, TestedCloud Rack, TestedCloud AI, and TestedCloud Monitor.

## Current Repository Status

- [x] Repository located at `~/testedcloud-lab`
- [x] Branch confirmed as `main`
- [x] Working tree clean
- [x] Local branch up to date with `origin/main`
- [x] Documentation structure already exists

## Day 1 — Repository and Documentation Inventory

- [x] Confirm repository path
- [x] Confirm Git branch
- [x] Confirm Git status
- [x] Confirm latest commits
- [x] List existing documentation files
- [x] List project directories
- [x] Create current status document
- [x] Create pending issues document
- [ ] Commit Week 1 operational tracking files

## Day 2 — On-Prem Validation

- [x] Validate Docker containers
- [x] Validate Docker Compose services
- [x] Validate backend container logs
- [x] Validate NGINX container logs
- [x] Validate FastAPI health endpoint through TestedCloud NGINX
- [x] Validate NGINX reverse proxy endpoint
- [ ] Validate Cloudflare public endpoint if applicable
- [x] Save validation evidence

## Day 3 — GCP Event Pipeline Validation

- [x] Confirm active GCP project
- [x] List Pub/Sub topics
- [x] List Pub/Sub subscriptions
- [x] Validate Cloud Run consumer service
- [x] Review Cloud Run logs
- [x] Send test event
- [x] Confirm BigQuery insert
- [ ] Save validation evidence

## Day 4 — IAM and Security Validation

- [x] List service accounts
- [x] Confirm Cloud Run service identity
- [x] Confirm Pub/Sub push OIDC configuration
- [x] Review project IAM policy
- [x] Confirm private VM has no public IP
- [x] Confirm IAP firewall rule
- [x] Confirm Cloudflare Access protection
- [x] Save security validation evidence

## Day 5 — Repository Hygiene

- [x] Review `.gitignore`
- [x] Search for `.env` files
- [x] Search for private keys
- [x] Search for hardcoded secrets
- [x] Confirm no sensitive files are committed
- [x] Confirm documentation links are still valid

## Day 6 — README and Portfolio Review

- [x] Review main `README.md`
- [x] Confirm TestedCloud modules are listed
- [x] Confirm architecture docs are linked
- [x] Confirm evidence docs are referenced
- [x] Confirm roadmap is aligned with current modules

## Day 7 — Weekly Closure

- [ ] Update current status
- [ ] Update pending issues
- [ ] Commit final documentation changes
- [ ] Push to GitHub
- [ ] Confirm clean working tree
- [ ] Prepare Week 2 TestedCloud Chat stabilization plan
