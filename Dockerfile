# Stage 1: Build the JAR file
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean install

# Stage 2: Create the final image
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/target/wsConnections-1.0.jar /app/app.jar

# Expose ports for the application and debugging
EXPOSE 8080

# Ensure logs directory exists and has appropriate permissions
RUN mkdir -p /app/logs && \
    chown -R 1000:0 /app && \
    chmod -R 775 /app/logs

# Set the entry point with JVM options for debugging and running the application
ENTRYPOINT ["java", "-Xms128m", "-Xmx512m", "-XX:+UseG1GC", "-XX:ParallelGCThreads=4", "-XX:G1HeapRegionSize=4m", "-XX:MaxMetaspaceSize=256m", "-agentlib:jdwp=transport=dt_socket,address=*:5005,server=y,suspend=n", "-jar", "/app/app.jar"]
