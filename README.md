# Account Microservice

Spring Boot Webflux microservice that handles account operations (accounts CRUD).

## Stack
- Java 11
- Spring Boot 2.x
- Spring Webflux
- Spring Cloud Config Client
- Reactive Mongodb
- Openapi contract first
- Swagger ui

## Configuration
Service connects to Config Server using:
```properties
spring.application.name=ms-account-service
spring.config.import=optional:configserver:http://localhost:8888
```
for properties
```yaml
account:
  config:
    checking:
      maintenanceFee: 5.0
    fixedterm:
      maxMonthlyMovements: 1
      availableDayForMovements: 20
    savings:
      maxMonthlyMovements: 5

application:
  config:
    customer-service-url: http://localhost:8090/api/v1/customers
```

## Swagger
http://localhost:8091/swagger-ui.html

![ms-account-service-2025-02-11-225336](https://github.com/user-attachments/assets/11b80cab-6f90-499b-848f-42f072391057)
