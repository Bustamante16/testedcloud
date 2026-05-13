# Repository Hygiene Validation — Week 1

Date: May 13, 2026
Scope: Git tracking, .gitignore, local runtime configuration, secret scanning, Firebase/Android configuration, and repository hygiene

## Objective

Validate that the TestedCloud repository does not track obvious secrets, private keys, service account credentials, local runtime configuration, or sensitive environment files.

## Git Status

Command:

    git status

Result:

    On branch main
    Your branch is up to date with origin/main.
    nothing to commit, working tree clean

Status:

    PASS

## .gitignore Review

Command:

    cat .gitignore

Validated ignore patterns include:

    .env
    .env.*
    *.env
    secrets/
    credentials/
    keys/
    *.key
    *.pem
    *.json
    service-account*.json
    *gcp*.json
    *credentials*.json
    frontend/config.js
    docker-compose.yml
    google-services.json
    GoogleService-Info.plist
    local.properties
    *.jks
    *.keystore
    .firebase/
    firebase-debug.log
    firestore-debug.log
    ui-debug.log

Status:

    PASS

## Potential Sensitive Files Search

Command:

    find . -type f with filters for .env, keys, credentials, secrets, tokens, and JSON files

Observed files:

    firebase/testedcloud-chat/firestore.indexes.json
    monitoring/policies/cloud-run-5xx-alert.json
    monitoring/policies/dlq-message-alert.json
    monitoring/policies/pubsub-backlog-alert.json

Finding:

    The observed JSON files are expected project configuration files and monitoring policy definitions.
    No service account key, credential file, private key, or environment file was observed in the search results.

Status:

    PASS

## Hardcoded Secret Keyword Search

Command:

    grep -RniE "api_key|apikey|secret|password|token|private_key|client_secret|TESTEDCLOUD_API_KEY|GOOGLE_APPLICATION_CREDENTIALS" .

Findings:

    backend/app.py references TESTEDCLOUD_API_KEY from an environment variable with a fallback placeholder.
    frontend/index.html references window.TESTEDCLOUD_CONFIG?.API_KEY.
    frontend/config.js contains a local runtime API key.
    docker-compose.yml contains local runtime environment values.
    Android files reference password variables for Firebase Authentication flows.
    Documentation contains expected references to secret hygiene, tokens, OIDC, and security practices.

Risk classification:

    frontend/config.js: Local runtime config only. Not tracked by Git.
    docker-compose.yml: Local runtime config only. Not tracked by Git.
    Android password references: Code variables only, no hardcoded real passwords.
    Documentation references: Expected and non-sensitive.

Status:

    PASS WITH OBSERVATION

## Tracked Sensitive Files Check

Command:

    git ls-files | grep -Ei "\.env$|\.key$|\.pem$|\.p12$|secret|credential|token|google-services\.json|service-account|application_default_credentials"

Result:

    No tracked files returned.

Status:

    PASS

## Local Runtime Config Tracking Check

Command:

    git ls-files docker-compose.yml frontend/config.js

Result:

    No tracked files returned.

Finding:

    docker-compose.yml and frontend/config.js exist locally but are not tracked by Git.
    This matches the repository hygiene strategy.

Status:

    PASS

## Docker Compose Runtime Configuration

Observed local runtime values:

    GCP_PROJECT_ID=majestic-layout-255620
    PUBSUB_TOPIC_ID=testedcloud-events
    GOOGLE_APPLICATION_CREDENTIALS=/root/.config/gcloud/application_default_credentials.json
    TESTEDCLOUD_API_KEY=<local value redacted>

Finding:

    docker-compose.yml is correctly ignored by Git.
    The tracked docker-compose.example.yml uses placeholders instead of real local values.

Status:

    PASS WITH OBSERVATION

## Frontend Runtime Configuration

Observed local runtime value:

    frontend/config.js includes a local API key value.

Finding:

    frontend/config.js is correctly ignored by Git.
    The tracked frontend/config.example.js uses a placeholder.

Status:

    PASS WITH OBSERVATION

## Firebase and Android Configuration Review

Observed files:

    firebase/testedcloud-chat/firestore.indexes.json
    apps/testedcloud-chat/app/src/main/AndroidManifest.xml
    apps/testedcloud-chat/gradle/gradle-daemon-jvm.properties
    apps/testedcloud-chat/gradle/wrapper/gradle-wrapper.properties
    apps/testedcloud-chat/gradle.properties

Findings:

    No private_key, client_secret, service account file, or Firebase service account credential was observed.
    Password references are normal application variables for Firebase Authentication.

Status:

    PASS

## Final Result

Repository hygiene validation result:

    PASS WITH OBSERVATIONS

The repository does not currently track obvious secrets, private keys, service account credentials, .env files, google-services.json, docker-compose.yml, or frontend/config.js.

## Follow-Up Improvements

- Keep docker-compose.yml and frontend/config.js untracked.
- Continue using docker-compose.example.yml and frontend/config.example.js for public documentation.
- Consider replacing the local shared API key approach with a stronger backend-auth or Cloudflare Access identity-aware pattern.
- Consider Secret Manager for production-style runtime configuration.
- Continue scanning before every major commit.
