FROM openjdk:8-jdk-slim AS build-env
ADD ./src /source
WORKDIR /source
RUN javac com/ericsson/bsf/hellokube/HelloKube.java
RUN jar cfe hellokube.jar com.ericsson.bsf.hellokube.HelloKube com/ericsson/bsf/hellokube/HelloKube.class resources/lyrics.txt

FROM gcr.io/distroless/java
COPY --from=build-env /source /root/java-exec
WORKDIR /root/java-exec
CMD ["hellokube.jar"]