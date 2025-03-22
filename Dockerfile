# Faza 1: Build Spring Boot aplikacije
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Faza 2: Runtime sa Nginx i Spring Boot
FROM nginx:alpine

# Kopiraj konfiguracijske fajlove
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY startup.sh /app/startup.sh

# Instaliraj zavisnosti
RUN apk add --no-cache openjdk21 netcat-openbsd && \
    chmod +x /app/startup.sh

# Kopiraj JAR iz build faze
COPY --from=builder /app/target/*.jar /app/app.jar

# Pokreni skriptu
CMD ["/app/startup.sh"]
