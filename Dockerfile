FROM maven:3.9.3-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copiamos los archivos necesarios
COPY pom.xml .
COPY src ./src

# Build del JAR
RUN mvn clean package -DskipTests

# Segunda etapa: runtime
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Copiamos el JAR compilado de la etapa anterior
COPY --from=build /app/target/control-empleados-1.0.jar app.jar

# Puerto que Render asigna
ENV PORT 8081
EXPOSE $PORT

ENTRYPOINT java -Dserver.port=$PORT -jar app.jar
