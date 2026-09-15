# Etapa de compilacion (Maven)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -q -DskipTests clean package -pl backend-core -am

# Etapa de ejecucion (JRE)
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/backend-core/target/banco-bff-core-1.0.0.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]