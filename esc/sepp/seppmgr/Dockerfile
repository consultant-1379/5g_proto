ARG DOCKER_BASE_IMG
FROM $DOCKER_BASE_IMG

RUN mkdir -p /seppmanager &&\
    chmod -R g=u /seppmanager

COPY target/docker/entrypoint.sh /java-exec/entrypoint.sh
COPY target/libs /java-exec/libs
COPY target/classes /java-exec/classes

WORKDIR /seppmanager

# # UID calculated with the input "eric-sepp-manager"; using the hashing function provided by ADP Design Rule 122:
# # https://eteamspace.internal.ericsson.com/display/AA/Kubernetes+Security+Design+Rules#KubernetesSecurityDesignRules-DR-D1123-122
# RUN echo "292193:x:292193:292193:An Identify for eric-sepp-manager:/nonexistent:/bin/false" >>/etc/passwd &&\
#     echo "292193:!::0:::::" >>/etc/shadow &&\
#     chown -R 292193:0 /java-exec && chmod -R g=u /java-exec &&\
#     chown -R 292193:0 /worker/config/logcontrol && chmod -R g=u /worker/config/logcontrol &&\
#     chown -R 292193:0 /mnt && chmod -R g=u /mnt

# # UID calculated with the input "eric-sepp-manager"; using the hashing function provided by ADP Design Rule 122:
# # https://eteamspace.internal.ericsson.com/display/AA/Kubernetes+Security+Design+Rules#KubernetesSecurityDesignRules-DR-D1123-122
RUN echo "292193:x:292193:292193:An Identify for eric-sepp-manager:/nonexistent:/bin/false" >>/etc/passwd
RUN echo "292193:!::0:::::" >>/etc/shadow && \
    chown -R 292193:0 /java-exec && chmod -R g=u /java-exec && \
    mkdir /vertx && chown -R 151802:0 /vertx && chmod -R g=u /vertx && \
    chown -R 292193:0 /mnt && chmod -R g=u /mnt

USER 292193
ENTRYPOINT ["/bin/bash", "-c", "/java-exec/entrypoint.sh java -cp /java-exec/libs/*:/java-exec/classes ${JVM_PARAMS} -Dlogback.configurationFile=/java-exec/classes/${LOGBACK_FILENAME} com.ericsson.sc.sepp.manager.SeppManager"]
