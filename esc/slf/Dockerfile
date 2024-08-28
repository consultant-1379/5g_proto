ARG DOCKER_BASE_IMG
FROM $DOCKER_BASE_IMG

COPY target/docker/entrypoint.sh /java-exec/entrypoint.sh
COPY target/libs /java-exec/libs
COPY target/classes /java-exec/classes


# UID calculated with the input "eric-sc-slf" using the hashing function provided by ADP DR 122
#https://eteamspace.internal.ericsson.com/display/AA/Kubernetes+Security+Design+Rules#KubernetesSecurityDesignRules-DR-D1123-122
RUN mkdir -p /slf &&\
    echo "216816:x:216816:216816:An Identify for eric-sc-slf:/nonexistent:/bin/false" >>/etc/passwd &&\
    echo "216816:!::0:::::" >>/etc/shadow &&\
    mkdir -p /slf && chown -R 216816:0 /slf && chmod -R g=u /slf &&\
    chown -R 216816:0 /java-exec && chmod -R g=u /java-exec &&\
    mkdir /vertx && chown -R 216816:0 /vertx && chmod -R g=u /vertx && \
    chown -R 216816:0 /mnt && chmod -R g=u /mnt

WORKDIR /slf

USER 216816

ENTRYPOINT ["/bin/bash", "-c", "/java-exec/entrypoint.sh java -cp /java-exec/libs/*:/java-exec/classes ${JVM_PARAMS} -Dlogback.configurationFile=/java-exec/classes/${LOGBACK_FILENAME} com.ericsson.sc.slf.SlfWorker"]
