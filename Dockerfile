
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=build /build/target/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java",\
  "-XX:+UseContainerSupport",\
  "-XX:MaxRAMPercentage=75.0",\
  "-XX:InitialRAMPercentage=25.0",\
  "-Dfile.encoding=UTF-8",\
  "-jar", \
  "app.jar"]