ARG CBOS_IMG
FROM $CBOS_IMG

ARG CBOS_HARDENING_ARCHIVE
ARG CBOS_HARDENING_REPO
ARG CBOS_JAVA
ARG CBOS_OPENSSL
ARG CBOS_REPO
#Hadolint DL4006
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
RUN zypper addrepo --gpgcheck-strict $CBOS_REPO CBOS_REPO && \
    zypper --gpg-auto-import-keys refresh -f &&\
    zypper --no-refresh --quiet install --no-recommends --auto-agree-with-licenses --no-confirm CBOS_REPO:curl \
                                                                                                $CBOS_JAVA \
                                                                                                $CBOS_OPENSSL \
                                                                                                CBOS_REPO:xz &&\
    curl -sSf "${CBOS_HARDENING_REPO}/${CBOS_HARDENING_ARCHIVE}" | tar xz &&\
    zypper --non-interactive remove --clean-deps curl xz &&\
    zypper clean -a &&\
    zypper removerepo CBOS_REPO &&\
    chmod 750 /cbo-harden.sh &&\
    /cbo-harden.sh &&\
    rm /cbo-harden.sh && \
    mkdir -p /java-exec

WORKDIR /java-exec
