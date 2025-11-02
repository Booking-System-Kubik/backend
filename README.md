# Office Booking System - Deployment Guide

- Docker and Docker Compose installed
- Java Runtime Environment

## Tech Stack

```bash
Java 21 (LTS)

Spring Boot 3.5.4

PostgreSQL 15

Hibernate 6.2+

Maven

Prometheus

Grafana
```


## Deployment Steps

### 1. Start Infrastructure Services

1)
Create .env file in the root directory
Example:
```bash
ADMIN_EMAIL=admin@company.com
ADMIN_PASSWORD=superartios22021980
ADMIN_FULLNAME=Admin Adminov
ADMIN_POSITION=Admin

DB_URL=jdbc:postgresql://postgres:5432/myapp_db
DB_NAME=myapp_db
DB_USERNAME=user
DB_PASSWORD=pass

JWT_SECRET=AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8=
JWT_ACCESS_EXPIRATION_MIN=60
JWT_REFRESH_EXPIRATION_DAYS=7
```


2)
```bash
docker-compose up --build -d
```



