# -------------------------------
# Etapa 1: Build
# -------------------------------
FROM maven:3.9.3-jdk-21 AS build

WORKDIR /app

# Copiamos pom.xml primero para cache de dependencias
COPY pom.xml .

# Copiamos el código fuente
COPY src ./src

# Construimos el JAR sin tests
RUN mvn clean package -DskipTests

# -------------------------------
# Etapa 2: Runtime
# -------------------------------
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copiamos el JAR desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Puerto dinámico para Render
EXPOSE 8080

# Comando para ejecutar la app con Spring profile "production"
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=production","app.jar"]


