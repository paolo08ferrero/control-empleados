# Usamos JDK 21 Alpine, liviano
FROM eclipse-temurin:21-jdk-alpine

# Carpeta de trabajo dentro del contenedor
WORKDIR /app

# Copiamos el JAR de tu proyecto al contenedor
COPY target/control-empleados-1.0.jar.original app.jar

# Exponemos el puerto (Render asigna $PORT automáticamente)
EXPOSE 8081

# Comando para ejecutar la app
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-Dserver.port=$PORT","-jar","app.jar"]
