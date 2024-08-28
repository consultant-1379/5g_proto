ARG DOCKER_BASE_IMG
FROM $DOCKER_BASE_IMG

EXPOSE 8080

RUN mkdir -p /worker/config/logcontrol &&\
    chmod -R g=u /worker/config/logcontrol

COPY target/docker/entrypoint.sh /java-exec/entrypoint.sh
COPY target/libs /java-exec/libs
COPY target/classes /java-exec/classes

WORKDIR /worker

RUN echo "169581:x:169581:169581:An Identify for non-rootroot-cont:/nonexistent:/bin/false" >>/etc/passwd &&\
    echo "169581:!::0:::::" >>/etc/shadow && chown -R 169581:0 /java-exec && chmod -R g=u /java-exec &&\
    mkdir /vertx && chown -R 169581:0 /vertx && chmod -R g=u /vertx && chown -R 169581:0 /worker/config/logcontrol &&\
    chmod -R g=u /worker/config/logcontrol && chown -R 169581:0 /mnt && chmod -R g=u /mnt

USER 169581
ENTRYPOINT ["/bin/bash", "-c", "/java-exec/entrypoint.sh java --add-opens java.base/java.net=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED -cp /java-exec/libs/*:/java-exec/classes ${JVM_PARAMS} -Dlogback.configurationFile=/java-exec/classes/${LOGBACK_FILENAME} com.ericsson.esc.bsf.worker.BsfWorker"]
