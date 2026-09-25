FROM openjdk:17-jdk-alpine
COPY target/hosteo-0.0.1-SNAPSHOT.jar hosteo-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/hosteo-0.0.1-SNAPSHOT.jar"]
