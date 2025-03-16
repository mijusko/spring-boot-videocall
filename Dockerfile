FROM maven:3.8.8-openjdk-21
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests
# Preimenuj jar fajl – uzmi bilo koji .jar u target direktorijumu
RUN mv target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
