# TestedCloud Public Portfolio Hosting

## 1\. Purpose

This document describes the public hosting architecture for the TestedCloud portfolio landing page.

The goal is to clearly separate the public portfolio surface from the protected operational lab UI.

Final model:

```text
testedcloud.com
    |
    v
GitHub Pages
    |
    v
Public portfolio landing page


ui.testedcloud.com
    |
    v
Cloudflare Access
    |
    v
Protected on-prem lab UI
```

This separation allows recruiters, hiring managers, and technical reviewers to see the public project story without exposing the operational lab directly.

## 2\. Public vs Protected Surfaces

|Domain|Purpose|Access Model|Hosting|
|-|-|-|-|
|`testedcloud.com`|Public portfolio landing page|Public|GitHub Pages|
|`www.testedcloud.com`|Optional public alias|Public|GitHub Pages|
|`ui.testedcloud.com`|Operational lab UI|Protected|Cloudflare Access + on-prem NUC|

Design principle:

```text
testedcloud.com = public portfolio story
ui.testedcloud.com = protected operational lab
```

## 3\. Why GitHub Pages

GitHub Pages was selected for the public landing page because:

* It is well known by technical communities and recruiters.
* It connects the public portfolio to the GitHub repository.
* It is appropriate for static HTML/CSS portfolio content.
* It avoids unnecessary backend or container hosting for a simple landing page.
* It keeps the public portfolio separate from the protected lab UI.
* It provides a clean story for interviews and portfolio reviews.

The landing page is a static site and does not require:

* Cloud Run
* Backend API
* Database
* Authentication
* Server-side code

## 4\. Source Files

The public landing page source is stored in:

```text
portfolio/
```

Main file:

```text
portfolio/index.html
```

Custom domain file:

```text
portfolio/CNAME
```

Expected content of `portfolio/CNAME`:

```text
testedcloud.com
```

GitHub Pages deployment workflow:

```text
.github/workflows/pages.yml
```

## 5\. Deployment Method

GitHub Pages is deployed using GitHub Actions.

Workflow:

```text
.github/workflows/pages.yml
```

Deployment source:

```text
portfolio/
```

High-level workflow:

```text
Push to main
    |
    v
GitHub Actions workflow
    |
    v
Upload portfolio/ as Pages artifact
    |
    v
Deploy to GitHub Pages
    |
    v
testedcloud.com
```

## 6\. GitHub Pages Workflow

The workflow deploys the `portfolio/` folder to GitHub Pages.

Important workflow behavior:

* Runs on pushes to `main`
* Runs when files under `portfolio/\*\*` change
* Runs when `.github/workflows/pages.yml` changes
* Can also be triggered manually using `workflow\_dispatch`
* Deploys only the public portfolio folder, not the protected lab UI

This prevents the operational UI under `frontend/` from being published as the public landing page.

## 7\. Domain Verification

The domain `testedcloud.com` was verified in GitHub Pages by creating a DNS TXT record in Cloudflare.

Verification TXT record:

```text
Name: \_github-pages-challenge-Bustamante16
Type: TXT
Value: GitHub-provided verification value
```

The verification value is not included in this document to keep the documentation sanitized.

Purpose:

* Proves control of `testedcloud.com`
* Reduces risk of domain takeover
* Allows GitHub Pages to use the custom domain

## 8\. Cloudflare DNS Design

Cloudflare remains the DNS provider for `testedcloud.com`.

Root domain records for GitHub Pages:

```text
A  testedcloud.com  185.199.108.153
A  testedcloud.com  185.199.109.153
A  testedcloud.com  185.199.110.153
A  testedcloud.com  185.199.111.153
```

Recommended proxy status for GitHub Pages records:

```text
DNS only
```

Optional `www` record:

```text
CNAME  www  testedcloud.com
```

Recommended proxy status for `www`:

```text
DNS only
```

Protected lab UI record:

```text
ui.testedcloud.com
```

This remains connected through Cloudflare Tunnel / Cloudflare Access and should remain protected.

## 9\. Previous Root Redirect

Before the public portfolio landing page was deployed, the root domain redirected to:

```text
ui.testedcloud.com
```

Previous behavior:

```text
testedcloud.com
    |
    v
ui.testedcloud.com
```

This was useful during early lab development, but it was not ideal for the final portfolio design because the public root domain should present the project story, not redirect immediately to the protected operational UI.

Updated behavior:

```text
testedcloud.com
    |
    v
GitHub Pages public landing page
```

The protected operational UI remains available from the landing page through a button:

```text
Open Protected Lab UI
```

Target:

```text
https://ui.testedcloud.com
```

## 10\. Security Separation

This hosting model improves security posture by separating:

|Surface|Risk Level|Access|
|-|-|-|
|Public portfolio|Low|Public static content|
|Protected lab UI|Higher|Cloudflare Access required|
|Local runtime config|Sensitive|Not committed to Git|
|Backend API key|Sensitive|Local config only|
|Monitoring notification channel IDs|Sensitive|Redacted in committed policy definitions|

Security benefits:

* The public landing page does not expose operational controls.
* The lab UI remains protected by Cloudflare Access.
* Visitors can learn about the architecture without touching the real lab.
* The public page can safely link to the protected UI.
* The protected UI still requires authorization.

## 11\. Public Landing Page Content

The landing page explains:

* What TestedCloud is
* Hybrid architecture overview
* On-prem to Google Cloud event flow
* Security and IAM hardening
* Cloudflare Access protection
* Cloud Monitoring alerts
* Evidence-based validation
* Cost-conscious design
* Roadmap for TestedChat, industrial telemetry, BGP, and Vertex AI

The landing page intentionally avoids exposing:

* API keys
* Cloudflare secrets
* Notification channel IDs
* Billing account IDs
* Service account private keys
* Local runtime configuration
* Private credentials

## 12\. Protected Lab Button

The public landing page includes a button:

```text
Open Protected Lab UI
```

Target:

```text
https://ui.testedcloud.com
```

Expected behavior:

* Authorized users are allowed through Cloudflare Access.
* Unauthorized users see Cloudflare Access authentication or denial.
* The public page itself remains safe to expose.

This reinforces the portfolio story:

```text
Public story at testedcloud.com
Protected working lab at ui.testedcloud.com
```

## 13\. Validation Performed

Validated behavior:

|Validation|Result|
|-|-|
|GitHub Pages workflow completed|Successful|
|`portfolio/index.html` deployed|Successful|
|`portfolio/CNAME` configured|Successful|
|GitHub Pages source set to GitHub Actions|Successful|
|`testedcloud.com` loads landing page|Successful|
|Root redirect to `ui.testedcloud.com` no longer blocks landing page|Successful|
|`ui.testedcloud.com` remains separate and protected|Successful|
|Public landing page contains protected lab button|Successful|

## 14\. Evidence

Recommended evidence file:

```text
docs/evidence/github-pages-validation.txt
```

Suggested evidence content:

```text
TestedCloud GitHub Pages Validation

Public portfolio domain: https://testedcloud.com
Hosting platform: GitHub Pages
Source folder: portfolio/
Deployment method: GitHub Actions
Custom domain file: portfolio/CNAME
Custom domain: testedcloud.com

Validated behavior:
- testedcloud.com loads the public portfolio landing page.
- ui.testedcloud.com remains the protected operational lab UI.
- The landing page includes a button to open the protected lab UI.
- Cloudflare root redirect to ui.testedcloud.com was removed or bypassed for the public landing page.
- GitHub Pages deployment workflow completed successfully.

Security / separation model:
- testedcloud.com is public.
- ui.testedcloud.com remains protected by Cloudflare Access.
- The public landing page does not expose secrets or operational credentials.

Status: Validated
```

## 15\. Recommended Validation Commands

From a terminal:

```bash
dig testedcloud.com +short
```

Expected GitHub Pages IPs:

```text
185.199.108.153
185.199.109.153
185.199.110.153
185.199.111.153
```

Check HTTP headers:

```bash
curl -I https://testedcloud.com
```

Expected result:

```text
HTTP/2 200
```

or another successful HTTPS response from GitHub Pages.

Check protected UI:

```bash
curl -I https://ui.testedcloud.com
```

Expected result depends on Cloudflare Access behavior, but the endpoint should remain separate from the public GitHub Pages landing page.

## 16\. Operational Notes

Do not publish the operational UI from:

```text
frontend/
```

The `frontend/` folder is the protected lab UI source.

The public landing page should remain in:

```text
portfolio/
```

Do not commit:

```text
docker-compose.yml
frontend/config.js
.env
service account keys
Cloudflare tokens
notification channel IDs
billing account IDs
```

## 17\. Troubleshooting

### Issue: `testedcloud.com` still redirects to `ui.testedcloud.com`

Check Cloudflare for old redirect rules:

```text
Rules → Redirect Rules
Rules → Page Rules
Bulk Redirects
```

Disable or remove root redirects from:

```text
testedcloud.com
```

to:

```text
ui.testedcloud.com
```

### Issue: `testedcloud.com` shows old Squarespace content

Check Cloudflare DNS for old root records.

The root domain should point to GitHub Pages A records:

```text
185.199.108.153
185.199.109.153
185.199.110.153
185.199.111.153
```

Remove conflicting old root records.

### Issue: GitHub Pages HTTPS is not ready

Wait for GitHub to issue the certificate after DNS is correct.

Then enable:

```text
Enforce HTTPS
```

in GitHub Pages settings when available.

### Issue: GitHub Pages workflow does not deploy

Check:

```text
GitHub → Repo → Actions
```

Confirm the workflow:

```text
Deploy TestedCloud portfolio to GitHub Pages
```

completed successfully.

## 18\. Interview Explanation

A concise way to explain this architecture:

> I separated the public portfolio from the operational lab. The root domain, `testedcloud.com`, is hosted on GitHub Pages as a static public landing page. The actual lab UI remains at `ui.testedcloud.com` and is protected by Cloudflare Access. This lets recruiters and technical reviewers see the architecture story publicly while keeping the working lab behind identity-aware access.

A more technical version:

> The public site is deployed from the `portfolio/` folder using GitHub Actions and GitHub Pages, with `portfolio/CNAME` mapping the custom domain `testedcloud.com`. Cloudflare DNS points the root domain to GitHub Pages using the GitHub Pages A records. The protected lab UI remains on `ui.testedcloud.com` through Cloudflare Access and the existing on-prem tunnel. This separates static public content from the operational lab surface.

## 19\. Customer Engineer Relevance

This design is relevant to Customer Engineer and Cloud Architect roles because it demonstrates:

* Clear separation of public and protected surfaces
* DNS and custom domain configuration
* GitHub Actions-based static site deployment
* Cloudflare DNS and Access awareness
* Security-conscious portfolio presentation
* Cost-conscious hosting for static content
* Operational clarity for recruiters and reviewers
* Ability to communicate architecture through a public-facing site

## 20\. Final Positioning

The public portfolio hosting model completes the external presentation layer of TestedCloud.

It provides a clean public entry point at:

```text
testedcloud.com
```

while keeping the real operational lab protected at:

```text
ui.testedcloud.com
```

This strengthens the overall portfolio by combining public storytelling, secure access design, GitHub-based deployment, and evidence-based architecture documentation.

