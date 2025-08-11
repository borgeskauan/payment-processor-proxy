cd C:\CentralHub\Personal\Studies\rinha-backend\rinha-de-backend-2025\rinha-test
cd /home/tyrael/CentralHub/Studies/rinha-backend/rinha-de-backend-2025/rinha-test

**Run k6 tests**
$env:K6_WEB_DASHBOARD="true"; $env:K6_WEB_DASHBOARD_EXPORT="html-report.html"; k6 run rinha.js

psql -U postgres

SELECT * FROM payments WHERE requested_at BETWEEN '2025-08-09T19:16:19.218Z' AND '2025-08-09T19:16:32.718Z' ORDER BY requested_at DESC LIMIT 10;

SELECT COUNT(*) FROM payments WHERE requested_at BETWEEN '2025-08-09T19:16:19.218Z' AND '2025-08-09T19:16:32.718Z';

from: 2025-08-09T19:16:19.218Z, to: 2025-08-09T19:16:32.718Z;

curl --unix-socket /tmp/spring-boot.sock -X POST   http://localhost/payments   -H "Content-Type: application/json"   -d '{
"amount": 100.00,
"correlationId": "5ad15dc5-8b16-4e09-a519-10c294702ab3"
}'

curl --unix-socket /tmp/spring-boot.sock -X GET   http://localhost/payments-summary