# Account Microservice

Spring Boot Webflux microservice that handles customer operations.

## Stack
- Java 11
- Spring Boot 2.x
- Spring Webflux
- Spring Cloud Config Client
- Reactive Mongodb
- Openapi contract first

## Configuration
Service connects to Config Server for properties:
```properties
spring.application.name=ms-account-service
spring.config.import=optional:configserver:http://localhost:8888