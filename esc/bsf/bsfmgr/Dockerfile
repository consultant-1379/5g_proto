ARG DOCKER_BASE_IMG
FROM $DOCKER_BASE_IMG

EXPOSE 8080

RUN mkdir -p /manager &&\
    chmod -R g=u /manager

COPY target/docker/entrypoint.sh /java-exec/entrypoint.sh
COPY target/libs /java-exec/libs
COPY target/classes /java-exec/classes

WORKDIR /manager

RUN echo "278885:x:278885:278885:An Identify for non-rootroot-cont:/nonexistent:/bin/false" >>/etc/passwd &&\
    echo "278885:!::0:::::" >>/etc/shadow && chown -R 278885:0 /java-exec && chmod -R g=u /java-exec &&\
    mkdir /vertx && chown -R 278885:0 /vertx && chmod -R g=u /vertx && chown -R 278885:0 /mnt && chmod -R g=u /mnt
USER 278885


ENTRYPOINT ["/bin/bash", "-c", "/java-exec/entrypoint.sh java -cp /java-exec/libs/*:/java-exec/classes ${JVM_PARAMS} -Dlogback.configurationFile=/java-exec/classes/${LOGBACK_FILENAME} com.ericsson.esc.bsf.manager.BsfManager"]
