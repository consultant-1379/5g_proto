ARG DOCKER_BASE_IMG
FROM $DOCKER_BASE_IMG

EXPOSE 8080

COPY target/docker/entrypoint.sh /java-exec/entrypoint.sh
COPY target/libs /java-exec/libs
COPY target/classes /java-exec/classes

RUN echo "109347:x:109347:109347:An Identify for non-rootroot-cont:/nonexistent:/bin/false" >>/etc/passwd &&\
    echo "109347:!::0:::::" >>/etc/shadow && chown -R 109347:0 /java-exec && chmod -R g=u /java-exec &&\
    mkdir /vertx && chown -R 109347:0 /vertx && chmod -R g=u /vertx
USER 109347

ENTRYPOINT ["/bin/bash", "-c", "/java-exec/entrypoint.sh java -cp /java-exec/libs/*:/java-exec/classes ${JVM_PARAMS} -Dlogback.configurationFile=/java-exec/classes/${LOGBACK_FILENAME} com.ericsson.sc.bsf.diameter.BsfDiameter"]
