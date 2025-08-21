# Usamos JDK 21 Alpine
FROM eclipse-temurin:21-jdk-alpine

# Instalamos Maven
RUN apk add --no-cache maven

# Carpeta de trabajo
WORKDIR /app

# Copiamos pom.xml primero para aprovechar cache de Docker
COPY pom.xml .

# Copiamos el código fuente
COPY src ./src

# Construimos el JAR dentro del contenedor
RUN mvn clean package spring-boot:repackage -DskipTests

# Copiamos el JAR generado a app.jar
RUN cp target/control-empleados-1.0.jar app.jar

# Exponemos el puerto (Render asigna $PORT)
EXPOSE 8081

# Comando para ejecutar la app
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-Dserver.port=$PORT","-jar","app.jar"]
