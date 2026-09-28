# --- Stage 1 : Build ---
FROM eclipse-temurin:23-jdk-alpine AS builder
WORKDIR /app

# Copie des fichiers Maven pour mettre en cache les dépendances
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B

# Copie du code source et compilation
COPY src ./src
RUN ./mvnw clean package -DskipTests

# --- Stage 2 : Runtime ---
FROM eclipse-temurin:23-jre-alpine
WORKDIR /app

# Utilisateur non-root pour la sécurité
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copie du JAR compilé
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]