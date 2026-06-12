# ETAPA 1: Construcción (Build)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
# Copiamos el pom y descargamos dependencias
COPY pom.xml .
# Copiamos el código fuente
COPY src ./src
# Compilamos saltándonos los tests para ir más rápido
RUN mvn clean package -DskipTests

# ETAPA 2: Ejecución (Run)
FROM eclipse-temurin:21-jre
WORKDIR /app
# Copiamos solo el .jar final generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar
# Exponemos el puerto de Spring Boot
EXPOSE 8080
# Comando para arrancar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]