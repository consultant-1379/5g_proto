FROM openjdk:21-jdk-slim
COPY ./target/certm-cr-handler-test-0.0.5-jar-with-dependencies.jar /usr/src/testcrhandler/
WORKDIR /usr/src/testcrhandler
CMD ["java", "-jar", "certm-cr-handler-test-0.0.5-jar-with-dependencies.jar"]
