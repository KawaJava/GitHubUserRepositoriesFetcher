# GitHubUserRepositoriesFetcher
Application allows you to find repositories detalis of chosen  GitHub user.
The information you can explore is: owner login, repository name and for all repository branches - their names and last commit sha.
Application is written in Java 21 and Spring Boot and is based on Github API. Application was tested by unit tests for service and integration tests for controller.

![Java](https://img.shields.io/badge/21-Java-orange?style=for-the-badge)
![Spring Boot](https://img.shields.io/badge/3-Spring%20Boot-brightgreen?style=for-the-badge)
![WebFlux](https://img.shields.io/badge/WebFlux-red?style=for-the-badge)
![Mockito](https://img.shields.io/badge/Mockito-yellow?style=for-the-badge)
![AssertJ](https://img.shields.io/badge/AssertJ-blue?style=for-the-badge)
![JUnit](https://img.shields.io/badge/JUnit-purple?style=for-the-badge)

## Technologies Used

- **Java 21**: The programming language used for this project.
- **Spring Boot 6**: A framework used to create stand-alone, production-grade Spring-based applications.
- **WebFlux**: A reactive web framework included in Spring Boot.
- **Mockito**: A mocking framework for unit tests in Java.
- **AssertJ**: A library providing a rich set of assertions.
- **JUnit**: A framework for unit testing in Java.

## Run

Clone the project

```bash
git clone https://github.com/KawaJava/GitHubUserRepositoriesFetcher.git
```
## Build the project using Maven or Gradle:
```bash
./mvnw clean install
```

## Usage

When your application is running, use localhost:8080/USERNAME where USERNAME is github username - for instance, for getting my repositories details, send localhost:8080/kawajava. 
If user don't exist, application will send a proper information.
