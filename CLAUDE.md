# Nail Salon Booking System — Project Plan

This file is the persistent context for Claude Code on this project. Read it fully before doing anything.

## Who this is for and why

Aurel is a newly graduated developer (Bachelor in IT and Management, USN) job hunting for junior backend/fullstack roles in Norway. This project exists to get real, defensible hands-on Spring Boot experience for his CV and technical interviews. **Goal: a working, deployed app he can show and explain in interviews.** He knows Java fundamentals (OOP, separation of concerns, polymorphism) but lacks hands-on Spring Boot experience and has not worked with it consistently.

## Working mode: hybrid vibe coding

Aurel has switched from strict learning mode to hybrid vibe coding. Claude writes the code, including entities, services, controllers, DTOs, availability logic and booking validation. Speed matters now, but he still has to be able to defend this project in interviews.

Rules:
- Build in small vertical slices (one feature at a time, following the build order below). Do not dump many features in one go.
- After each slice, give a SHORT explanation (5 to 10 lines max, plain language): what was added, how the pieces connect, and the one or two design decisions an interviewer might ask about (for example why ddl-auto=validate, why price is an int, why a DTO instead of returning the entity).
- Make sure it compiles and runs before calling a slice done. Test endpoints with curl and show the result.
- Suggest a descriptive commit message per slice. Never commit secrets.
- The availability calculation (step 5) is the interview centerpiece: write it as pure, unit-tested Java and walk Aurel through it more carefully than the rest.
- If Aurel asks "why" about anything, explain fully. Debugging help is always welcome.
- Keep the code simple and readable over clever. Prefer standard Spring Boot patterns he will see at work.

## Tech stack

- Java 21, Spring Boot (Maven), package base `no.aurel.backend`, but artifact/branding is "nailsbyag" (sister's business, Instagram: nailsByAg)
- PostgreSQL 16 via Docker Compose (already chosen over embedded H2 — matches production setup)
- Flyway for migrations (ddl-auto=validate, NEVER update/create — this is intentional and he should be able to explain why)
- Lombok, Spring Validation, Spring Data JPA
- Spring Security added LATER (Phase 5+), not at the start — avoid blocking early development with auth
- Frontend: React + Vite + TypeScript (added after backend vertical slice works)
- Deploy target: Railway or Render, free tier, env vars for secrets

## Repo layout (monorepo)

```
nailsalon/
  backend/     <- Spring Boot project (pom.xml lives here)
  frontend/    <- React, added later
  compose.yaml
  README.md
  CLAUDE.md    <- this file
```

Current status: build order step 1 done (Postgres via Docker, Spring Boot connects, Flyway V1 creates `salon_service`). Next: step 2. Dependencies: Spring Web, Spring Data JPA, PostgreSQL Driver, Validation, Flyway Migration, Lombok, Spring Boot DevTools.

## Domain model (target)

- **SalonService** (table `salon_service`, NOT `service` — avoids collision with Spring's @Service): name, description, durationMinutes, priceNok (int, never a float/double — money), active
- **Employee**: name, maybe specialties
- **Customer**: name, phone, email
- **Appointment**: links Customer + Employee + SalonService, start time, status
- **OpeningHours / EmployeeSchedule**: for the availability calculation later

Package-by-feature, not package-by-layer, e.g. `no.aurel.backend.salonservice` contains its own entity, repository, service, controller, dto/.

## Build order (do not skip ahead)

1. Docker Postgres running, Spring Boot connects, Flyway runs first migration — CHECKPOINT: app boots clean on port 8080
2. First full vertical slice: SalonService entity → repository → DTOs → service → controller → tested with curl. This is the template for every feature after.
3. Employee, Customer entities (same pattern, faster)
4. Appointment entity with relations (@ManyToOne) — slow down here, this is new territory
5. Availability calculation — the interview centerpiece, pure Java, unit-testable
6. Booking validation + custom exceptions + @RestControllerAdvice
7. Spring Security (customer vs admin roles) — only now
8. Tests: unit tests for availability logic, @WebMvcTest for controllers
9. React frontend
10. Deploy

## Git / GitHub habits

- Repo is public on GitHub (`nailsalon`), intentional — portfolio value matters more than hiding code
- NEVER commit secrets: DB passwords, API keys, .env files. Root `.gitignore` already covers `.env`, `application-local.properties`, `application-prod.properties`, `target/`, `.idea/`
- Commit per meaningful chunk with descriptive messages ("Add Appointment entity and repository"), not "update" or "final"
- Local Postgres creds (`nailsalon` / `localdev`) are fine to have in compose.yaml since they only work on localhost — do not use these for anything production

## After each phase

Remind Aurel (one line) to add 2 or 3 sentences to `NOTES.md` in his own words about what was built and why. This is his interview prep material. Don't write it for him.
