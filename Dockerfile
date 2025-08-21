# Etapa 1: Build
FROM maven:3.9.3-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos los archivos de configuración y código
COPY pom.xml .
COPY src ./src

# Compilamos el proyecto, sin tests para acelerar
RUN mvn clean package -DskipTests

# Etapa 2: Runtime
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Copiamos el JAR reempaquetado desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Configuramos el puerto dinámico que Render asigna
ENV SERVER_PORT=$PORT
EXPOSE $PORT

# Comando para ejecutar la aplicación
ENTRYPOINT ["java","-jar","app.jar"]
