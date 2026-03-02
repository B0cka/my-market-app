# 1. Сборка
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Копируем РОДИТЕЛЬСКИЙ pom.xml в корень /app
COPY pom.xml .

# Копируем pom.xml модуля в подпапку
COPY payment-service/pom.xml payment-service/

# Копируем исходники модуля
COPY payment-service/src payment-service/src

# Запускаем сборку, указывая конкретный pom файл (-f)
# Теперь Maven видит родителя в /app/pom.xml и ребенка в /app/payment-service/pom.xml
RUN mvn -f payment-service/pom.xml clean package -DskipTests

# 2. Запуск
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Забираем JAR из папки модуля
COPY --from=builder /app/payment-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]