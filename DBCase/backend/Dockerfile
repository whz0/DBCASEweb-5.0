FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
COPY config ./config
RUN mvn clean package -q

FROM eclipse-temurin:25-jre
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8443
CMD ["java", "-jar", "app.jar"]
