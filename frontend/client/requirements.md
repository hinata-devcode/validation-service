## Packages
axios | Required for custom API configuration with ngrok headers
uuid | Required for generating Idempotency-Key headers
@types/uuid | Type definitions for uuid

## Notes
Using custom axios instance for all API calls to handle ngrok warnings and CORS
Idempotency keys generated on client side for validation requests
Polling implemented manually via recursive setTimeout as requested
