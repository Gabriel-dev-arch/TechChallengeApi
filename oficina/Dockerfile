# ---- build ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN for i in 1 2 3; do mvn -B -q dependency:go-offline && break || (echo "Retrying dependency download ($i)..." && sleep 5); done
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---- runtime ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /workspace/target/*.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
