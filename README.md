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
eureka:
  instance:
    hostname: localhost
    instance-id: ${spring.application.name}:${random.int}
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
      
spring:
  data:
    mongodb:
      host: localhost
      port: 27017
      database: ms-bootcamp-arcelles

server:
  port: ${PORT:0}

application:
  config:
    customer-service-url: http://ms-customer-service/api/v1/customers
    credit-service-url: http://ms-credit-service/api/v1/credits
    accounts:
      checking:
        maintenanceFee: 5
        maxMonthlyMovementsNoFee: 5
        transactionCommissionFeePercentage: 5
      fixedterm:
        maxMonthlyMovements: 1
        availableDayForMovements: 20
        maxMonthlyMovementsNoFee: 5
        transactionCommissionFeePercentage: 5
      savings:
        maxMonthlyMovements: 30
        maxMonthlyMovementsNoFee: 5
        transactionCommissionFeePercentage: 5

management:
  endpoints:
    web:
      exposure:
        include: health,circuitbreakerevents
  endpoint:
    health:
      show-details: always

resilience4j:
  circuitbreaker:
    instances:
      customerService:
        slidingWindowSize: 3
        failureRateThreshold: 100
        waitDurationInOpenState: 10000
        permittedNumberOfCallsInHalfOpenState: 3
        ignoreExceptions:
          - com.project1.ms_account_service.exception.NotFoundException
          - com.project1.ms_account_service.exception.BadRequestException
      creditService:
        slidingWindowSize: 3
        failureRateThreshold: 100
        waitDurationInOpenState: 10000
        permittedNumberOfCallsInHalfOpenState: 3
        ignoreExceptions:
          - com.project1.ms_account_service.exception.NotFoundException
          - com.project1.ms_account_service.exception.BadRequestException
  timelimiter:
    instances:
      customerService:
        timeoutDuration: 2s
        cancelRunningFuture: true
      creditService:
        timeoutDuration: 2s
        cancelRunningFuture: true

springdoc:
  api-docs:
    path: /account-docs/v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
  webjars:
    prefix:
```

## Swagger
http://localhost:8091/swagger-ui.html

![ms-account-service-2025-03-14-152653](https://github.com/user-attachments/assets/954954f9-4f27-4141-a830-94bf6e6938c0)

