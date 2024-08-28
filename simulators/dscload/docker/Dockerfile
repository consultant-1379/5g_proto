ARG DOCKER_BASE_IMG
FROM $DOCKER_BASE_IMG

ARG CBOS_REPO
ARG ADP_DEV_REPO
RUN zypper addrepo --gpgcheck-strict $CBOS_REPO CBOS_REPO && \
    zypper addrepo --gpgcheck-strict $ADP_DEV_REPO ADP_DEV_REPO &&\
    zypper addrepo --gpgcheck-strict https://arm.sero.gic.ericsson.se/artifactory/proj-suse-repos-rpm-local/SLE15/ISO/SLE-15-SP5-GM/SLE-15-SP5-Full/SLE-15-SP5-Full-x86_64-GM-Media1/Module-Basesystem/ SLES15_SP5_BASE &&\
    zypper --gpg-auto-import-keys refresh -f &&\
    zypper --no-refresh --quiet install --no-recommends --auto-agree-with-licenses --no-confirm CBOS_REPO:bzip \
                                                                                                CBOS_REPO:ca-certificates \
                                                                                                CBOS_REPO:ca-certificates-mozilla \
                                                                                                CBOS_REPO:curl \
                                                                                                CBOS_REPO:glibc \
                                                                                                CBOS_REPO:less \
                                                                                                CBOS_REPO:lksctp-tools \
                                                                                                CBOS_REPO:libltdl7 \
                                                                                                CBOS_REPO:libopenssl1_0_0 \
                                                                                                CBOS_REPO:openssh \
                                                                                                CBOS_REPO:patch \
                                                                                                CBOS_REPO:perl \
                                                                                                CBOS_REPO:python3 \
                                                                                                CBOS_REPO:python3-pip \
                                                                                                CBOS_REPO:sysvinit-tools \
                                                                                                CBOS_REPO:unzip \
                                                                                                CBOS_REPO:wget \
                                                                                                CBOS_REPO:xz \
                                                                                                CBOS_REPO:zip \
                                                                                                ADP_DEV_REPO:libasan8 \
                                                                                                ADP_DEV_REPO:libatomic1 \
                                                                                                ADP_DEV_REPO:libtool \
                                                                                                ADP_DEV_REPO:vim \
                                                                                                SLES15_SP5_BASE:libwireshark15 \
                                                                                                SLES15_SP5_BASE:telnet \
                                                                                                SLES15_SP5_BASE:wireshark &&\
    zypper clean -a

ARG TOKEN
ENV TOKEN=$TOKEN
ARG LOADTOOL_VERSION
ENV LOADTOOL_VERSION=$LOADTOOL_VERSION
ENV DSC_LOAD_URL=https://arm.sero.gic.ericsson.se/artifactory/proj-dsc-test-generic-local/DSC/Builds/Upload-DscLoad7/dsc_load7_dlt_$LOADTOOL_VERSION

# Create dsc_load directory and assign permissions.
# Download dsc_load binary and make it executable.
# Upgrade pip to the latest version.
# Install python libraries:
#   - requests
RUN mkdir -p /opt/dsc-load &&\
    chmod 777 -R /opt/dsc-load &&\
    mkdir -p /opt/dsc-load/rootCA &&\
    chmod 777 -R /opt/dsc-load/rootCA &&\
    curl -f -k -H "X-JFrog-Art-Api:$TOKEN" -O $DSC_LOAD_URL &&\
    mv dsc_load7_dlt_$LOADTOOL_VERSION /opt/dsc-load/dsc_load &&\
    chmod +x /opt/dsc-load/dsc_load &&\
    pip install --upgrade --trusted-host pypi.org --trusted-host files.pythonhosted.org pip &&\
    pip install requests

COPY docker/templates /opt/dsc-load/templates
COPY docker/configure_dscload.py /opt/dsc-load

WORKDIR /opt/dsc-load
CMD python3 /opt/dsc-load/configure_dscload.py &&\
    tail -f /dev/null
