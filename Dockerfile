# ---------- build stage ----------
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /app

# cache deps
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
RUN chmod +x mvnw
RUN ./mvnw -q -DskipTests dependency:go-offline

# build jar
COPY src src
RUN ./mvnw -DskipTests clean package

# ---------- run stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Render te setea PORT. Spring debe escuchar ese puerto.
ENV PORT=8080

COPY --from=build /app/target/*.jar app.jar

# arranca con el profile prod (también lo vas a setear en Render, pero acá queda fallback)
ENTRYPOINT ["java","-jar","app.jar"]