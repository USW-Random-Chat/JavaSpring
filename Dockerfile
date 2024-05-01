# Build stage using Gradle
FROM gradle:8.5-jdk11 as builder

WORKDIR /app

COPY build.gradle settings.gradle ./

COPY src ./src

RUN gradle bootJar --no-daemon

# Run stage
FROM openjdk:11-jdk

COPY --from=builder /app/build/libs/*.jar /app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app.jar"]