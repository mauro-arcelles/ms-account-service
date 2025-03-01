FROM maven:3.8-openjdk-11-slim AS maven

COPY ./pom.xml ./pom.xml
RUN mvn dependency:go-offline -B

COPY ./src ./src
RUN mvn package -DskipTests -Dcheckstyle.skip

FROM openjdk:11-jre-slim

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=maven target/*.jar app.jar
COPY --from=maven src/main/resources/ /app/resources/

ENTRYPOINT ["java","-jar","app.jar"]