FROM openjdk
WORKDIR /app
COPY server-0.0.1-SNAPSHOT.jar .
CMD ["java", "-jar", "./server-0.0.1-SNAPSHOT.jar"]