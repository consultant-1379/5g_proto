ARG DOCKER_BASE_IMG
FROM $DOCKER_BASE_IMG

RUN mkdir -p /monitor &&\
    chmod -R g=u /monitor

COPY target/docker/entrypoint.sh /monitor/bin/entrypoint.sh
COPY target/libs /monitor/libs
COPY target/classes /monitor/classes

RUN echo "291828:x:291828:291828:An Identify for non-rootroot-cont:/nonexistent:/bin/false" >>/etc/passwd &&\
    echo "291828:!::0:::::" >>/etc/shadow &&\
    chown -R 291828:0 /monitor && chmod -R g=u /monitor
RUN mkdir /vertx && chown -R 291828:0 /vertx && chmod -R g=u /vertx
USER 291828

WORKDIR /monitor

ENTRYPOINT ["/bin/bash", "-c", "/monitor/bin/entrypoint.sh java -cp /monitor/libs/*:/monitor/classes ${JVM_PARAMS} -Dlogback.configurationFile=/monitor/classes/${LOGBACK_FILENAME} com.ericsson.monitor.Monitor"]