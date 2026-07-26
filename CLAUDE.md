# Nail Salon Booking System — Project Plan

This file is the persistent context for Claude Code on this project. Read it fully before doing anything.

## Who this is for and why

Aurel is a newly graduated developer (Bachelor in IT and Management, USN) job hunting for junior backend/fullstack roles in Norway. This project exists to get real, defensible hands-on Spring Boot experience for his CV and technical interviews. **The primary goal is Aurel's learning, not fastest completion.** He knows Java fundamentals (OOP, separation of concerns, polymorphism) but lacks hands-on Spring Boot experience and has not worked with it consistently.

## Critical working rule for Claude Code

Aurel must be able to explain every line of domain logic in an interview. Follow this split strictly:

**Do NOT silently generate for him — explain first, let him write it himself:**
- JPA entities (SalonService, Employee, Customer, Appointment, etc.)
- Service layer / business logic
- The availability calculation (core feature — given a service + date, return bookable slots)
- Booking validation (no double-booking, no past-dates, opening hours)
- Controllers and DTOs

For these, prefer explaining the concept, showing ONE small example, and asking questions, over writing the full file for him. If he explicitly asks you to just write it, you can, but by default push him to type it himself and ask "do you want me to explain this or write it?"

**OK to generate directly, low learning value:**
- pom.xml / dependency config
- compose.yaml / Docker setup
- Flyway migration SQL syntax (explain briefly, but this is fine to generate)
- application.properties
- React/frontend styling and boilerplate
- Debugging help (this is actually high-value learning, always help fully here)
- Git commands, GitHub setup

Never let him commit a diff he can't explain back to you in plain language.

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

Current status: `backend/` scaffolded from start.spring.io, folder structure just fixed (was nested backend/backend, now flat). Dependencies: Spring Web, Spring Data JPA, PostgreSQL Driver, Validation, Flyway Migration, Lombok, Spring Boot DevTools.

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

Prompt Aurel to write a few sentences in `NOTES.md` (create if missing) explaining what he built and why, from memory, without looking at the code. This is his interview prep material. Don't write it for him.
