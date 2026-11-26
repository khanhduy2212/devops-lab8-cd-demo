# The JAR has already been built and tested by Maven in the CI stages.
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/lab8-java-ci-cd-1.0.0.jar /app/app.jar
USER 10001:10001
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "--add-modules", "jdk.httpserver", "-jar", "/app/app.jar"]
