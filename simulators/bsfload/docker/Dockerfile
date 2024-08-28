ARG DOCKER_BASE_IMG
FROM $DOCKER_BASE_IMG

ENV CONFIG_PATH=/opt/bsf-load/config.json \
    WORK_DIR=/opt/bsf-load

WORKDIR $WORK_DIR

# Add repos for curl, jq and vim installation.
# Refresh the repo.
# Install:
# - vim: To edit configuration files.
# - curl: To troubleshoot target end-points.
# - jq: To improve readability of json files.
# Clean.

ARG ADP_DEV_REPO
ARG CBOS_REPO
RUN zypper addrepo --gpgcheck-strict $ADP_DEV_REPO ADP_DEV_REPO && \
    zypper addrepo --gpgcheck-strict $CBOS_REPO CBOS_REPO && \
    zypper --gpg-auto-import-keys refresh -f &&\
    zypper --no-refresh --quiet install --no-recommends --auto-agree-with-licenses --no-confirm CBOS_REPO:java-17-openjdk-headless \
                                                                                                CBOS_REPO:libcrypt1 \
                                                                                                CBOS_REPO:openssl \
                                                                                                CBOS_REPO:curl \
                                                                                                CBOS_REPO:jq \
                                                                                                ADP_DEV_REPO:vim &&\
    zypper clean -a

COPY docker/configuration_samples $WORK_DIR/configuration_samples/
COPY docker/config.json docker/README.md $WORK_DIR/

COPY target/docker/entrypoint.sh /java-exec/entrypoint.sh
COPY target/libs /java-exec/libs
COPY target/classes /java-exec/classes

ENTRYPOINT ["/bin/bash", "-c", "/java-exec/entrypoint.sh java -cp /java-exec/libs/*:/java-exec/classes ${JVM_PARAMS} com.ericsson.esc.bsf.load.server.BsfLoad"]
