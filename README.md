# user-service

Spring Boot-mikroservicen för **användare och autentisering** i Niklas Bodega. Det här är den enda tjänsten som skapar JWT och hanterar inloggning.

**Port:** 8084  
**Databas:** egen MySQL (`users`)  
**Stack:** Java 21, Spring Boot, Spring Security, JPA, JWT, OAuth2 (Google + GitHub)

## Vad den här tjänsten gör

- Registrera konto (`POST /api/user/register`)
- Logga in med e-post/lösenord (`POST /api/auth/login`) — sätter httpOnly-cookien `jwt`
- Logga ut, även från alla enheter
- Hämta, uppdatera och radera den inloggade användaren (`/api/user`)
- OAuth2-inloggning via Google och GitHub (`/oauth2/**`)

User-service **äger inte** bokningar eller recensioner. Den frågar booking-service innan ett konto raderas, så att användare med aktiva bokningar inte tas bort.

## Vad de andra tjänsterna gör

| Tjänst | Ansvar |
|--------|--------|
| **booking-service** (8083) | Rum, rumstyper, tillgänglighet och bokningar |
| **review-service** (8086) | Recensioner och betyg per rumstyp |
| **frontend** (8087) | React-gränssnittet som anropar API:erna |

## Hur tjänsterna pratar med varandra

```
Frontend ──► user-service     inloggning, profil, JWT-cookie
Frontend ──► booking-service  rum och bokningar
Frontend ──► review-service   recensioner

user-service ──GET /api/bookings/active──► booking-service
  Vid DELETE /api/user: radering avbryts om användaren har aktiva bokningar.

booking-service ──GET /api/user──► user-service
  Innan en bokning skapas: kontrollera att användaren fortfarande finns.

review-service ──GET /api/user──► user-service
  När en recension skapas: hämta visningsnamn att spara på recensionen.
```

Alla backends delar samma `JWT_SECRET`. Booking och review validerar cookien själva; de anropar inte user-service för varje request.

Intern URL mot den här tjänsten i Docker: `http://user-service:8084`.

## Starta hela systemet

Tjänsten körs tillsammans med resten via Docker Compose i infra-repot. Clone alla repos som syskonmappar:

```
niklas-bodega/
├── niklas-bodega-infra/
├── user/                 ← du är här
├── booking/
├── review-service/
└── frontend/
```

```bash
docker network create proxy-network   # om nätverket inte redan finns
cd ../niklas-bodega-infra
cp .env.example .env                  # fyll i JWT, DB och interna URL:er
docker compose up --build
```

I `.env` ska user-service nås som:

```env
USER_INTERNAL_ADDRESS=http://user-service:8084
BOOKING_INTERNAL_ADDRESS=http://booking-service:8083
```

Öppna sedan http://localhost:8087 (frontend). User API ligger på http://localhost:8084.

Se [niklas-bodega-infra/README.md](../niklas-bodega-infra/README.md) för miljövariabler och portar.

## Köra bara den här tjänsten (IDE)

Kräver en MySQL-instans (t.ex. `db-user` från Compose, mappad till `localhost:3308`). I `application.properties` pekar default mot `localhost:3306`.

```bash
mvn spring-boot:run
```

OAuth-klient-id:n kan lämnas tomma om du bara testar e-post/lösenord.
