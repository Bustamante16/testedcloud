# Cloudflare Access Validation — Week 1

Date: May 13, 2026
Scope: Cloudflare Access protection for TestedCloud UI and public route behavior

## Objective

Validate that the public TestedCloud UI route is protected by Cloudflare Access and identify whether the main domain API path routes to the on-prem API.

## UI Route Validation

Command:

    curl -i https://ui.testedcloud.com/

Result:

    HTTP/2 302
    location: https://testedcloud.cloudflareaccess.com/cdn-cgi/access/login/ui.testedcloud.com...
    www-authenticate: Cloudflare-Access

Finding:

    The UI route redirects unauthenticated users to Cloudflare Access login.

Status:

    PASS

## Main Domain API Route Validation

Command:

    curl -i https://testedcloud.com/api/health

Result:

    HTTP/2 404
    server: GitHub.com
    Page not found · GitHub Pages

Finding:

    The testedcloud.com/api/health route is currently served by GitHub Pages, not by the on-prem TestedCloud API.

Interpretation:

    This is not an API health failure. It indicates that testedcloud.com is currently associated with the portfolio/GitHub Pages route, while ui.testedcloud.com is protected by Cloudflare Access.

Status:

    OBSERVATION

## Final Result

Cloudflare Access validation result:

    PASS WITH OBSERVATION

The TestedCloud UI route is protected by Cloudflare Access. The testedcloud.com/api/health route should not be used as the primary API validation endpoint unless DNS and Cloudflare routing are intentionally updated to point that path to the on-prem API.
