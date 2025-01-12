FROM openjdk:8-jdk-alpine
ADD target/FraudDetectionSystem-1.0-SNAPSHOT.jar /FraudDetectionSystem-1.0-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "/FraudDetectionSystem-1.0-SNAPSHOT.jar"]