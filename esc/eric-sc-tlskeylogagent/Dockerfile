ARG DOCKER_BASE_IMG
FROM $DOCKER_BASE_IMG

RUN mkdir -p /logcontrol &&\
    chmod -R g=u /logcontrol

COPY target/docker/entrypoint.sh /java-exec/entrypoint.sh
COPY target/libs /java-exec/libs
COPY target/classes /java-exec/classes


RUN echo "198596:x:198596:198596:An Identify for non-rootroot-cont:/nonexistent:/bin/false" >>/etc/passwd &&\
    echo "198596:!::0:::::" >>/etc/shadow && chown -R 198596:0 /java-exec && chmod -R g=u /java-exec

USER 198596
ENTRYPOINT ["/bin/bash", "-c", "/java-exec/entrypoint.sh java -cp /java-exec/libs/*:/java-exec/classes ${JVM_PARAMS} -Dlogback.configurationFile=/java-exec/classes/${LOGBACK_FILENAME} com.ericsson.sc.keyexporter.TlsKeylogAgent"]
