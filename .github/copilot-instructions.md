# Repository guidance for Copilot

- Read [`docs/architecture.md`](../docs/architecture.md) for the component map
  and [`docs/booking-flow.md`](../docs/booking-flow.md) for current runtime
  behavior before changing the booking flow.
- Treat the Java source as authoritative if documentation and code differ.
  Check relevant call sites and data flow instead of assuming a class name or
  comment describes current behavior.
- This application can create a real appointment on the Luxmed portal. Do not
  trigger a live booking or use real credentials for tests unless explicitly
  authorized.
- Never read, reproduce, or commit values from
  `src/main/resources/config.properties`. It is local and gitignored. Use
  `config_default.properties` for key names and placeholders only.
- Preserve existing user changes. Keep edits scoped to the request, and update
  the relevant docs when behavior changes.
- Build with `mvn compile`. This repository currently has no automated test
  suite or linter configuration. Do not claim a live portal flow was tested
  unless it actually was.
