# Build Stage
FROM maven:3.10.2-eclipse-temurin-27 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests package

# Runtime Stage
FROM eclipse-temurin:27-jre

WORKDIR /app

COPY --from=build /app/target/golf-club-api-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]