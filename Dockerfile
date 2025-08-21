FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Construimos el JAR dentro del contenedor
COPY pom.xml .
COPY src ./src

RUN ./mvnw clean package -DskipTests

# Copiamos el JAR reempaquetado
RUN cp target/control-empleados-1.0.jar app.jar

# Exponemos un puerto genérico, Render usa $PORT
EXPOSE 8081

# Shell form para que $PORT se expanda
ENTRYPOINT java -XX:MaxRAMPercentage=75 -Dserver.port=$PORT -jar app.jar

