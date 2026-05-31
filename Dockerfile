FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw ./
RUN chmod +x mvnw

RUN ./mvnw -q -B dependency:go-offline

COPY src ./src
RUN ./mvnw -q -B clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN groupadd --system --gid 1001 appgroup && \
    useradd --system --uid 1001 --gid 1001 --home-dir /app appuser && \
    mkdir -p /app/logs /app/temp && \
    chown -R appuser:appgroup /app

COPY --from=build /workspace/target/Boutique-0.0.1-SNAPSHOT.jar /app/app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]