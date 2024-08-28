FROM armdocker.rnd.ericsson.se/proj-5g-bsf/envoy/eric-scp-envoy-base-debug:1.28.0-2

#FROM envoyproxy/envoy:v1.27.2
#RUN apt-get update && apt-get install -y curl tree vim tshark net-tools

#RUN zypper ar -C -G -f https://arm.rnd.ki.sw.ericsson.se/artifactory/proj-ldc-repo-rpm-local/common_base_os/sles/3.5.0-13?ssl_verify=no LDC-SLES15 && \
#    zypper ref -f -r LDC-SLES15 && \
##    zypper --quiet in --no-recommends -l -y curl && \
#    zypper --quiet in --no-recommends -l -y net-tools && \
##    zypper --quiet in --no-recommends -l -y tcpdump && \
#    zypper clean -a
#
#RUN zypper addrepo -C -G -f https://arm.rnd.ki.sw.ericsson.se/artifactory/proj-ldc-repo-rpm-local/adp-dev/adp-build-env/1.22.1?ssl_verify=no CBO_BUILD_REPO &&\
#    zypper --quiet in --no-recommends -l -y CBO_BUILD_REPO:gdb &&\
#    zypper clean -a
#
COPY entr /usr/local/bin/entr
ENTRYPOINT ["/bin/sleep", "infinity"]
