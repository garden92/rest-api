# KOL REST API Project

This is a Spring Boot REST API project with a multi-module Maven structure.

## AI Assistant Instructions

- **모든 응답을 한국어로 제공하세요**
- **답변은 항상 한글로 작성해주세요**

## Project Structure

- `app/` - Main Spring Boot application module
- `common/` - Common utilities and shared code
- `rest-api/` - REST API controllers and services

## Key Technologies

- Java Spring Boot
- Maven (multi-module project)
- BMON integration for monitoring

## Development Commands

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run -pl app

# Run tests
mvn test
```

## Project Context

This appears to be a KOL (Key Opinion Leader) REST API service that includes:
- Gateway pre-check functionality
- BMON sender integration for monitoring
- Route management
- Multi-environment configuration (dev/sit/prd)