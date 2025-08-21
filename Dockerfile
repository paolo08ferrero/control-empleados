# Usamos JDK 21 Alpine, liviano
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY target/control-empleados-1.0.jar.original app.jar

EXPOSE 8081

ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-Dserver.port=$PORT","-jar","app.jar"]
