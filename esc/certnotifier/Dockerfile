ARG DOCKER_BASE_IMG
FROM $DOCKER_BASE_IMG

COPY target/docker/entrypoint.sh /java-exec/entrypoint.sh
COPY target/libs /java-exec/libs
COPY target/classes /java-exec/classes

RUN echo "119407:x:119407:119407:An Identify for non-rootroot-cont:/nonexistent:/bin/false" >>/etc/passwd &&\
    echo "119407:!::0:::::" >>/etc/shadow && chown -R 119407:0 /java-exec && chmod -R g=u /java-exec &&\
    mkdir /vertx && chown -R 119407:0 /vertx && chmod -R g=u /vertx && chown -R 119407:0 /tmp && chmod -R g=u /tmp

USER 119407

ENTRYPOINT ["/bin/bash", "-c", "/java-exec/entrypoint.sh java -cp /java-exec/libs/*:/java-exec/classes ${JVM_PARAMS} -Dvertx.cacheDirBase=/vertx/vertx-cache -Dio.netty.native.workdir=/vertx -Dlogback.configurationFile=/java-exec/classes/${LOGBACK_FILENAME} com.ericsson.sc.certnotifier.CertificateNotifier"]
