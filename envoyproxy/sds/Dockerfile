ARG DOCKER_BASE_IMG
FROM $DOCKER_BASE_IMG

COPY target/docker/entrypoint.sh /java-exec/entrypoint.sh
COPY target/libs /java-exec/libs
COPY target/classes /java-exec/classes

# UID calculated with the input "eric-sc-sds"; using the hashing function provided by ADP Design Rule 122:
# https://eteamspace.internal.ericsson.com/display/AA/Kubernetes+Security+Design+Rules#KubernetesSecurityDesignRules-DR-D1123-122
RUN echo "267153:x:267153:267153:An Identify for eric-sc-sds:/nonexistent:/bin/false" >>/etc/passwd &&\
    echo "267153:!::0:::::" >>/etc/shadow &&\
    chown -R 267153:0 /java-exec && chmod -R g=u /java-exec &&\
    mkdir /vertx && chown -R 267153:0 /vertx && chmod -R g=u /vertx &&\
    chown -R 267153:0 /mnt && chmod -R g=u /mnt

USER 267153
ENTRYPOINT ["/bin/bash", "-c", "umask 002 && /java-exec/entrypoint.sh java -cp /java-exec/libs/*:/java-exec/classes ${JVM_PARAMS} -Dlogback.configurationFile=/java-exec/classes/${LOGBACK_FILENAME} com.ericsson.esc.scp.sds.SDSserver"]
