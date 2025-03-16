# Currency exchange app

This is a Spring Boot-based application that provides a simple API for currency exchange.

## Features
- Create new user API with firstName, lastName and initial funds in PLN   
- Currency exchange API USD <-> PLN
- Get account details API with balance
- API documentation available in swagger under http://localhost:8080/swagger-ui/index.html

## Prerequisites
- JDK 21
- Maven
- mongoDB

## Setup and Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/my-spring-boot-app.
   
2. You can configure DB data in application.properties file
    ```properties
    spring.data.mongodb.host=localhost
    spring.data.mongodb.port=27017
    spring.data.mongodb.database=currency
    spring.data.mongodb.username=admin
    spring.data.mongodb.password=password
    spring.data.mongodb.authentication-database=admin
   
3. build and run project:
     ```bash
   mvn clean install 
   mvn spring-boot:run

## Tests
Application contains mvc tests to verify REST API and unit tests for business logic

- How to run tests:

    ```bash
    mvn clean test