cd C:\CentralHub\Personal\Studies\rinha-backend\rinha-de-backend-2025\rinha-test

**Run k6 tests**
$env:K6_WEB_DASHBOARD="true"; $env:K6_WEB_DASHBOARD_EXPORT="html-report.html"; k6 run rinha.js

psql -U postgres

SELECT * FROM payments WHERE requested_at BETWEEN '2025-08-09T14:06:29.107Z' AND '2025-08-09T14:06:42.607Z' ORDER BY requested_at DESC LIMIT 10;

SELECT COUNT(*) FROM payments WHERE requested_at BETWEEN '2025-08-09T14:06:29.107Z' AND '2025-08-09T14:06:42.607Z';

from: 2025-08-09T14:06:29.107Z, to: 2025-08-09T14:06:42.607Z;