cd C:\CentralHub\Personal\Studies\rinha-backend\rinha-de-backend-2025\rinha-test

**Run k6 tests**
$env:K6_WEB_DASHBOARD="true"; $env:K6_WEB_DASHBOARD_EXPORT="html-report.html"; k6 run rinha.js