# Stage 1: Build
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

# 1. Copy hanya file yang diperlukan untuk build
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY src src

# 2. Download dependencies terlebih dahulu (cache layer)
RUN ./mvnw dependency:go-offline -B

# 3. Build aplikasi
RUN ./mvnw package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# 4. Copy hanya file JAR yang diperlukan
COPY --from=builder /app/target/*.jar app.jar

# 5. Gunakan entrypoint yang lebih aman
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]