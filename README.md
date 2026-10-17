# The Beauty Room by AG: Nail Salon Booking System

Online booking system for a nail salon in Drammen. Customers book appointments online, and the salon owner manages services and schedule. Live in production.

**Live site:** https://thebeautyroombyag.no

![Booking flow](docs/booking.png)

## Features

- Online booking with service selection and available time slots
- Automatic SMS confirmation to the customer (Twilio)
- Email notifications (Resend)
- Admin side for managing services and schedule
- Database migrations versioned with Flyway

## Stack

- **Backend:** Java 21, Spring Boot, Spring Data JPA
- **Database:** PostgreSQL 16, Flyway migrations
- **Frontend:** React + Vite
- **Infrastructure:** Docker (local dev), deployed on Railway
- **Integrations:** Twilio (SMS), Resend (email)

## Running locally

1. `docker compose up -d`
2. `cd backend && ./mvnw spring-boot:run`
3. API at http://localhost:8080

Copy `.env.example` to `.env` and fill in your own keys for SMS and email.

## What I learned

Taking a project from localhost to a live production system: deployment, environment variables and secrets, and integrating third-party services for SMS and email.
