ARG DOCKER_BASE_IMG
FROM $DOCKER_BASE_IMG

RUN mkdir -p /scpmanager &&\
    chmod -R g=u /scpmanager

COPY target/docker/entrypoint.sh /java-exec/entrypoint.sh
COPY target/libs /java-exec/libs
COPY target/classes /java-exec/classes

WORKDIR /scpmanager

# UID calculated with the input "eric-scp-manager" using the hashing function provided by ADP DR 122
#https://eteamspace.internal.ericsson.com/display/AA/Kubernetes+Security+Design+Rules#KubernetesSecurityDesignRules-DR-D1123-122

RUN echo "151802:x:151802:151802:An Identity for eric-scp-manager:/nonexistent:/bin/false" >>/etc/passwd
RUN echo "151802:!::0:::::" >>/etc/shadow && \
    chown -R 151802:0 /java-exec && chmod -R g=u /java-exec && \
    mkdir /vertx && chown -R 151802:0 /vertx && chmod -R g=u /vertx && \
    chown -R 151802:0 /mnt && chmod -R g=u /mnt

USER 151802

ENTRYPOINT ["/bin/bash", "-c", "/java-exec/entrypoint.sh java -cp /java-exec/libs/*:/java-exec/classes ${JVM_PARAMS} -Dlogback.configurationFile=/java-exec/classes/${LOGBACK_FILENAME} com.ericsson.sc.scp.manager.ScpManager"]
