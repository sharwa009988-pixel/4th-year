FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
# Copy backend module files
COPY pom.xml .
COPY src ./src
# Build the Spring Boot jar (skip tests to speed up deploys)
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
# Tune JVM heap conservatively for Render free tier
ENV JAVA_OPTS="-Xms256m -Xmx512m"
# Copy built jar from previous stage
COPY --from=build /app/target/java-fullstack-interview-prep-1.0.0.jar app.jar
# Render injects PORT; Spring Boot is configured to use ${PORT}
EXPOSE 10000
CMD ["bash", "-lc", "java $JAVA_OPTS -jar app.jar"]
