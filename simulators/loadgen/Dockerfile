ARG DOCKER_BASE_IMG
FROM $DOCKER_BASE_IMG as builder

ARG ADP_DEV_REPO
ARG CBOS_REPO
ARG CBOS_REPO_DEVEL
RUN zypper addrepo --gpgcheck-strict $ADP_DEV_REPO ADP_DEV_REPO &&\
    zypper addrepo --gpgcheck-strict $CBOS_REPO CBOS_REPO &&\
    zypper addrepo --gpgcheck-strict $CBOS_REPO_DEVEL CBOS_REPO_DEVEL &&\
    zypper --gpg-auto-import-keys refresh -f &&\
    zypper --no-refresh --quiet install --no-recommends --auto-agree-with-licenses --no-confirm CBOS_REPO:awk \
                                                                                                CBOS_REPO:jemalloc \
                                                                                                CBOS_REPO:diffutils \
                                                                                                CBOS_REPO:git-core \
                                                                                                ADP_DEV_REPO:gcc12 \
                                                                                                ADP_DEV_REPO:gcc12-c++ \
                                                                                                ADP_DEV_REPO:make &&\
    zypper clean -a

RUN ln -s /usr/bin/gcc-12 /usr/bin/gcc &&\
    ln -s /usr/bin/g++-12 /usr/bin/g++

FROM builder as redis
WORKDIR /build
RUN git clone --depth 1 https://github.com/redis/redis.git --branch 7.0.8
WORKDIR /build/redis
RUN make CC=gcc &&\
    make install

FROM builder as memtier
RUN zypper --no-refresh --quiet install --no-recommends --auto-agree-with-licenses --no-confirm ADP_DEV_REPO:automake \
                                                                                                ADP_DEV_REPO:autoconf \
                                                                                                ADP_DEV_REPO:libtool \
                                                                                                CBOS_REPO:pcre-devel \
                                                                                                CBOS_REPO:zlib-devel \
                                                                                                CBOS_REPO_DEVEL:libevent-devel &&\
    zypper clean -a

WORKDIR /build
RUN git clone --depth 1 https://github.com/RedisLabs/memtier_benchmark.git --branch 1.3.0
WORKDIR /build/memtier_benchmark
RUN autoreconf -ivf &&\
    ./configure --disable-tls &&\
    make &&\
    make install

FROM $DOCKER_BASE_IMG as final
ARG CBOS_REPO_DEVEL
ARG CBOS_REPO
RUN zypper addrepo --gpgcheck-strict $CBOS_REPO CBOS_REPO &&\
    zypper addrepo --gpgcheck-strict $CBOS_REPO_DEVEL CBOS_REPO_DEVEL &&\
    zypper --gpg-auto-import-keys refresh -f &&\
    zypper --no-refresh --quiet install --no-recommends --auto-agree-with-licenses --no-confirm CBOS_REPO:java-17-openjdk-headless \
                                                                                                CBOS_REPO:libcrypt1 \
                                                                                                CBOS_REPO:openssl \
                                                                                                CBOS_REPO_DEVEL:libevent &&\
    zypper clean -a

COPY --from=redis /usr/local/bin/ /usr/local/bin/
COPY --from=memtier /usr/local/bin/ /usr/local/bin/
COPY --from=memtier /usr/local/share/ /usr/local/share/

EXPOSE 80
EXPOSE 443
EXPOSE 8080

COPY target/docker/entrypoint.sh /java-exec/entrypoint.sh
COPY target/libs /java-exec/libs
COPY target/classes /java-exec/classes

ENTRYPOINT ["/bin/bash", "-c", "/java-exec/entrypoint.sh java -cp /java-exec/libs/*:/java-exec/classes ${JVM_PARAMS} com.ericsson.sim.loadgen.LoadGenerator"]
