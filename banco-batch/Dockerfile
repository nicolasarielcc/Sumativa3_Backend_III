# Etapa de compilacion (Maven)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -q -DskipTests clean package -pl banco-batch -am

# Etapa de ejecucion (JRE)
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/banco-batch/target/banco-bff-batch-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]