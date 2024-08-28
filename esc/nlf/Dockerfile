ARG DOCKER_BASE_IMG
FROM $DOCKER_BASE_IMG

COPY target/docker/entrypoint.sh /java-exec/entrypoint.sh
COPY target/libs /java-exec/libs
COPY target/classes /java-exec/classes


RUN mkdir -p /nlf &&\
    echo "156593:x:156593:156593:An Identify for eric-sc-nlf:/nonexistent:/bin/false" >>/etc/passwd &&\
    echo "156593:!::0:::::" >>/etc/shadow &&\
    mkdir -p /nlf && chown -R 156593:0 /nlf && chmod -R g=u /nlf &&\
    chown -R 156593:0 /java-exec && chmod -R g=u /java-exec &&\
    mkdir /vertx && chown -R 156593:0 /vertx && chmod -R g=u /vertx && \
    chown -R 156593:0 /mnt && chmod -R g=u /mnt


WORKDIR /nlf

USER 156593


ENTRYPOINT ["/bin/bash", "-c", "/java-exec/entrypoint.sh java -cp /java-exec/libs/*:/java-exec/classes ${JVM_PARAMS} -Dlogback.configurationFile=/java-exec/classes/${LOGBACK_FILENAME} com.ericsson.sc.nlf.NlfWorker"]
