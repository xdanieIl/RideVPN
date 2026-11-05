# RideVPN

Spring Boot / Thymeleaf web portal for RideVPN.

## Run

Requires Java 21, PostgreSQL, and the existing WireGuard server setup. Set
`DB_PASSWORD`, `WG_SERVER_PUBLIC_KEY`, and `WG_ENDPOINT`, then run `./mvnw spring-boot:run`
(Windows: `mvnw.cmd spring-boot:run`).

Register at `/register`, sign in at `/`, and continue to `/dashboard`.
Dashboard, settings, account, and API routes require authentication. Browser
forms use Spring Security sessions and CSRF protection; sign-out is a POST.

## Portal

- Dashboard: supplied map layout and an authenticated WireGuard configuration download.
  Import `ridevpn.conf` into your WireGuard client and activate it there. Downloading
  a new config replaces the previous peer key. Assigned IP is not live connection status.
- Account: real username, email, membership date, email updates, and password changes
  with current-password verification. Self-service account deletion is disabled pending
  a complete peer-revocation workflow.
- Settings: light/dark theme saved in this browser and shared with sign-in/sign-up.
  Auto-connect and kill-switch settings belong in the WireGuard client. DNS remains
  the server's existing Cloudflare setting; unsupported controls are disabled.

`/api/config` now accepts **POST**, with a CSRF token, because generating a config
rotates the user's peer key. It returns a no-store `ridevpn.conf` attachment. Clients
that previously called GET must update. HTTP Basic remains available for `/api/me`;
it does not bypass CSRF protection on mutation requests.

## Verify

Run `./mvnw test` (Windows: `mvnw.cmd test`). Tests use an isolated H2 database and a
mock WireGuard service; no production database or VPN peers are touched. Coverage
includes registration, login/logout sessions, private pages, CSRF, account updates,
and config download success/failure. Actual tunnel activation requires the deployed
Linux/WireGuard server and a client and is not exercised by these tests.
