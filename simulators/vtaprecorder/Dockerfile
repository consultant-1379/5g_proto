ARG DOCKER_BASE_IMG
FROM $DOCKER_BASE_IMG

ARG CBOS_REPO
ARG ADP_DEV_REPO
RUN zypper addrepo --gpgcheck-strict $CBOS_REPO CBOS_REPO && \
    zypper addrepo --gpgcheck-strict $ADP_DEV_REPO ADP_DEV_REPO && \
    zypper --gpg-auto-import-keys refresh -f &&\
    zypper --no-refresh --quiet install --no-recommends --auto-agree-with-licenses --no-confirm CBOS_REPO:libpcap CBOS_REPO:curl CBOS_REPO:tar ADP_DEV_REPO:tcpdump &&\
    zypper clean -a

COPY target/docker/entrypoint.sh /java-exec/entrypoint.sh
COPY target/libs /java-exec/libs
COPY target/classes /java-exec/classes
COPY dtls/dtlsServer /java-exec

ENV WORK_DIR=/opt/vtapconsumer
WORKDIR $WORK_DIR
COPY vtaprecorder $WORK_DIR/
RUN chmod 777 $WORK_DIR/vtaprecorder

ENTRYPOINT ["/bin/bash", "-c", "cd /java-exec/ && ./dtlsServer & /java-exec/entrypoint.sh java -cp /java-exec/libs/*:/java-exec/classes ${JVM_PARAMS} com.ericsson.sc.tapcol.recorder.VtapRecorder"]

EXPOSE 4754/UDP
EXPOSE 4755/UDP
EXPOSE 9999/TCP

