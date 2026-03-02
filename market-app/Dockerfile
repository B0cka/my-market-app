# 1. Сборка
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Копируем родителя
COPY pom.xml .

# Копируем модуль
COPY market-app/pom.xml market-app/
COPY market-app/src market-app/src

# Собираем
RUN mvn -f market-app/pom.xml clean package -DskipTests

# 2. Запуск
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/market-app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]