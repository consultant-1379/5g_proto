ARG BASE_IMAGE
FROM $BASE_IMAGE

ENV CC=gcc-12
ENV CXX=g++-12
RUN zypper --no-refresh --quiet install --no-recommends --auto-agree-with-licenses --no-confirm apr-devel \
                                                                                                apr-util-devel \
                                                                                                autoconf \
                                                                                                automake \
                                                                                                cmake \
                                                                                                glibc-devel \
                                                                                                libopenssl-devel \
                                                                                                libtool \
                                                                                                libunwind-devel \
                                                                                                ninja \
                                                                                                gcc12 \
                                                                                                gcc12-c++ \
                                                                                                go \
                                                                                                libopenssl-devel \
                                                                                                make \
                                                                                                openssl \
                                                                                                patch \
                                                                                                perl \
                                                                                                tar
