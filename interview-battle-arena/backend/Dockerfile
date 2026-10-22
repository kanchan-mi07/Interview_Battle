FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
# Do not run as root inside the container
RUN useradd --system --no-create-home appuser
COPY --from=build /app/target/*.jar app.jar
USER appuser
# Platforms such as Render inject PORT; this is only the fallback.
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]