# Expensify

Expensify is a full-stack expense tracking application built with a Java Spring Boot backend and a React frontend. It allows users to create, update, delete, and review expenses, and it includes OpenAI-powered analysis to summarize spending patterns and suggest improvements.

## Tech Stack

### Backend
- Java 21
- Spring Boot 4
- Spring Web
- Spring Data JPA
- MySQL
- Maven

### Frontend
- React
- Vite
- JavaScript
- CSS

### AI Integration
- OpenAI Responses API

## Repository Structure

```text
expensify/
|-- backend/
|   |-- src/main/java/com/expensify/backend/
|   |   |-- config/
|   |   |-- controller/
|   |   |-- dto/
|   |   |-- entity/
|   |   |-- exception/
|   |   |-- repository/
|   |   `-- service/
|   `-- src/main/resources/
|
|-- frontend/
|   |-- public/
|   `-- src/
|       |-- assets/
|       `-- services/
|
`-- README.md
```

## Features

- Add a new expense
- Update an existing expense
- Delete an expense
- List all expenses
- View category-wise totals
- Generate AI insights from expense data

## Architecture

The project follows a layered architecture on the backend and a component/service split on the frontend.

### Backend Architecture

- `controller`
  Exposes REST APIs for expense CRUD operations and AI analysis.
- `service`
  Contains business logic for expense management and OpenAI analysis.
- `repository`
  Handles database access using Spring Data JPA.
- `entity`
  Maps Java objects to database tables.
- `dto`
  Defines request and response payloads.
- `exception`
  Centralizes API error handling and custom exceptions.
- `config`
  Contains backend configuration such as CORS.

### Frontend Architecture

- `App.jsx`
  Main application screen and UI state management.
- `services/expenseApi.js`
  Handles API calls to the backend.
- `App.css` and `index.css`
  Contain styling for layout and components.

## How It Works

### Expense Flow

1. User enters expense details in the frontend.
2. React sends the request to the Spring Boot backend.
3. Spring Boot validates and stores the data in MySQL.
4. The frontend refreshes and shows the updated expense list.

### AI Analysis Flow

1. User clicks `Generate Insights`.
2. Frontend calls `/api/expenses/analysis`.
3. Backend collects stored expenses from MySQL.
4. Backend sends a structured prompt to the OpenAI Responses API.
5. OpenAI returns a spending summary and practical suggestions.
6. Frontend displays the analysis and category totals.

## Database

The application uses MySQL for the main runtime environment.

Main datasource configuration:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/expensedb?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD:root}
```

Tests use H2 so backend tests can run without requiring a local MySQL instance.

## Environment Variables

The backend expects these environment variables for local development:

```powershell
$env:DB_PASSWORD="your_mysql_password"
$env:OPENAI_API_KEY="your_openai_api_key"
```

## Run the Project

### 1. Start MySQL

Make sure MySQL is running on `localhost:3306`.

### 2. Start the Backend

```powershell
cd backend
$env:DB_PASSWORD="your_mysql_password"
$env:OPENAI_API_KEY="your_openai_api_key"
.\mvnw.cmd spring-boot:run
```

Backend runs on:

```text
http://localhost:8080
```

### 3. Start the Frontend

```powershell
cd frontend
npm install
npm run dev
```

Frontend runs on:

```text
http://localhost:5173
```

## API Endpoints

### Expense APIs

- `GET /api/expenses`
- `GET /api/expenses/{id}`
- `POST /api/expenses`
- `PUT /api/expenses/{id}`
- `DELETE /api/expenses/{id}`

### AI API

- `GET /api/expenses/analysis`

## Build and Test

### Backend

```powershell
cd backend
.\mvnw.cmd test
```

### Frontend

```powershell
cd frontend
npm run build
```

## Notes

- Do not store OpenAI keys directly in source files.
- Use environment variables for secrets.
- `spring.jpa.hibernate.ddl-auto=update` is convenient for development but should be reviewed before production deployment.
- CORS is configured for the Vite frontend running on `http://localhost:5173`.

## Future Improvements

- User authentication
- Expense filtering and search
- Monthly charts and dashboards
- Budget targets and alerts
- Export expenses to CSV or PDF
- Structured AI reports with trends and recommendations
