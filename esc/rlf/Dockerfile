ARG DOCKER_BASE_IMG
FROM $DOCKER_BASE_IMG

COPY target/docker/entrypoint.sh /java-exec/entrypoint.sh
COPY target/libs /java-exec/libs
COPY target/classes /java-exec/classes

RUN mkdir -p /rlf &&\
    echo "144455:x:144455:144455:An Identify for eric-sc-rlf:/nonexistent:/bin/false" >>/etc/passwd &&\
    echo "144455:!::0:::::" >>/etc/shadow &&\
    mkdir -p /rlf && chown -R 144455:0 /rlf && chmod -R g=u /rlf &&\
    chown -R 144455:0 /java-exec && chmod -R g=u /java-exec &&\
    mkdir /vertx && chown -R 144455:0 /vertx && chmod -R g=u /vertx && \
    chown -R 144455:0 /mnt && chmod -R g=u /mnt

WORKDIR /rlf

USER 144455

ENTRYPOINT ["/bin/bash", "-c", "/java-exec/entrypoint.sh java -cp /java-exec/libs/*:/java-exec/classes ${JVM_PARAMS} -Dlogback.configurationFile=/java-exec/classes/${LOGBACK_FILENAME} com.ericsson.sc.rlf.RlfWorker"]
