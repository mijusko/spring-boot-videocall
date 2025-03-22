# Faza 1: Build Spring Boot aplikacije
FROM openjdk:21-jdk-slim AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Faza 2: Runtime sa Nginx i Spring Boot
FROM nginx:alpine
WORKDIR /app

# Kopiraj Nginx konfiguraciju
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Instaliraj Java runtime
RUN apk add --no-cache openjdk21

# Kopiraj JAR iz build faze
COPY --from=builder /app/target/*.jar /app/app.jar

# Pokreni oba servisa paralelno
CMD (java -jar /app/app.jar &) && nginx -g "daemon off;"
