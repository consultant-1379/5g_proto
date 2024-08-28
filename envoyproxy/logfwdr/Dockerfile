ARG GO_BUILDER
ARG CBOS_IMG
FROM $GO_BUILDER as builder

ENV CGO_ENABLED=0
ENV GOARCH=amd64
ENV GOBIN="/build/eric-sc-logfwdr/go/bin"
ENV GOCACHE="/build/eric-sc-logfwdr/gocache"
ENV GOFLAGS=-mod=mod
ENV GOOS=linux
ENV GOPATH="/build/eric-sc-logfwdr/gopath"
ENV PATH="$PATH:/go/bin:/usr/local/go/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/fossa-cli:/build/eric-sc-logfwdr/go/bin:/build/eric-sc-logfwdr/protoc/bin"
#23.1
ENV PROTOC_VERSION=3.13.0
#1.4.0
ENV PROTOC_GEN_GO_VERSION=1.3.5
#1.4.10
ENV GOGOPROTO_VERSION=1.3.2

RUN go env &&\
    mkdir -p /build/eric-sc-logfwdr/go &&\
    mkdir -p /build/eric-sc-logfwdr/gopath/src &&\
    mkdir -p /build/eric-sc-logfwdr/gocache &&\
    mkdir -p /build/eric-sc-logfwdr/protoc &&\
    mkdir -p /logfwdr

COPY . /logfwdr
COPY ./rxgrpc-envoy-control-api /rxgrpc-envoy-control-api

WORKDIR /logfwdr/godeps
RUN echo 'Fetching GO dependencies ...' &&\
    go mod download &&\
    go mod verify &&\
    go mod graph &&\
    echo 'downloading protoc-gen-go binary, not supported by "go mod download"' &&\
    go install github.com/golang/protobuf/protoc-gen-go@v$PROTOC_GEN_GO_VERSION

WORKDIR /
RUN echo 'Installing protoc...' &&\
    zypper --no-refresh --quiet install --no-recommends --auto-agree-with-licenses --no-confirm  unzip &&\
    curl -LO -s https://github.com/protocolbuffers/protobuf/releases/download/v$PROTOC_VERSION/protoc-$PROTOC_VERSION-linux-x86_64.zip &&\
    zypper clean -a &&\
    unzip protoc-$PROTOC_VERSION-linux-x86_64.zip -d /build/eric-sc-logfwdr/protoc &&\
    rm -rf protoc-$PROTOC_VERSION-linux-x86_64.zip &&\
    protoc --version

RUN echo 'Starting protoc...' &&\
    protoc -I /build/eric-sc-logfwdr/gopath/pkg/mod/github.com/gogo/protobuf@v$GOGOPROTO_VERSION/protobuf -I rxgrpc-envoy-control-api/src/main/proto -I /build/eric-sc-logfwdr/gopath/pkg/mod/cache/download/github.com/cncf/udpa rxgrpc-envoy-control-api/src/main/proto/scp/api/v1/outlier.proto --go_out=plugins=grpc:/build/eric-sc-logfwdr/gopath/src &&\
    protoc -I /build/eric-sc-logfwdr/gopath/pkg/mod/github.com/gogo/protobuf@v$GOGOPROTO_VERSION/protobuf -I rxgrpc-envoy-control-api/src/main/proto -I /build/eric-sc-logfwdr/gopath/pkg/mod/cache/download/github.com/cncf/udpa rxgrpc-envoy-control-api/src/main/proto/scp/api/v1/healthcheck.proto --go_out=plugins=grpc:/build/eric-sc-logfwdr/gopath/src &&\
    protoc -I /build/eric-sc-logfwdr/gopath/pkg/mod/github.com/gogo/protobuf@v$GOGOPROTO_VERSION/protobuf -I rxgrpc-envoy-control-api/src/main/proto -I /build/eric-sc-logfwdr/gopath/pkg/mod/cache/download/github.com/cncf/udpa rxgrpc-envoy-control-api/src/main/proto/scp/service/outlier/v1/outlier.proto --go_out=plugins=grpc:/build/eric-sc-logfwdr/gopath/src &&\
    protoc -I /build/eric-sc-logfwdr/gopath/pkg/mod/github.com/gogo/protobuf@v$GOGOPROTO_VERSION/protobuf -I rxgrpc-envoy-control-api/src/main/proto -I /build/eric-sc-logfwdr/gopath/pkg/mod/cache/download/github.com/cncf/udpa rxgrpc-envoy-control-api/src/main/proto/envoy/data/cluster/v3/outlier_detection_event.proto --go_out=plugins=grpc,Mgoogle/protobuf/timestamp.proto=github.com/golang/protobuf/ptypes/timestamp,Mgoogle/protobuf/wrappers.proto=github.com/golang/protobuf/ptypes/wrappers:/build/eric-sc-logfwdr/gopath/src &&\
    protoc -I /build/eric-sc-logfwdr/gopath/pkg/mod/github.com/gogo/protobuf@v$GOGOPROTO_VERSION/protobuf -I rxgrpc-envoy-control-api/src/main/proto -I /build/eric-sc-logfwdr/gopath/pkg/mod/cache/download/github.com/cncf/udpa rxgrpc-envoy-control-api/src/main/proto/envoy/data/core/v3/health_check_event.proto --go_out=plugins=grpc,Mgoogle/protobuf/timestamp.proto=github.com/golang/protobuf/ptypes/timestamp,Mgoogle/protobuf/wrappers.proto=github.com/golang/protobuf/ptypes/wrappers:/build/eric-sc-logfwdr/gopath/src &&\
    protoc -I /build/eric-sc-logfwdr/gopath/pkg/mod/github.com/gogo/protobuf@v$GOGOPROTO_VERSION/protobuf -I rxgrpc-envoy-control-api/src/main/proto -I /build/eric-sc-logfwdr/gopath/pkg/mod/cache/download/github.com/cncf/udpa rxgrpc-envoy-control-api/src/main/proto/envoy/config/core/v3/address.proto --go_out=plugins=grpc,Mgoogle/protobuf/timestamp.proto=github.com/golang/protobuf/ptypes/timestamp,Mgoogle/protobuf/wrappers.proto=github.com/golang/protobuf/ptypes/wrappers:/build/eric-sc-logfwdr/gopath/src

RUN echo 'Creating a module of generated go files ...' &&\
    rm -Rf /build/eric-sc-logfwdr/gen &&\
    mkdir /build/eric-sc-logfwdr/gen &&\
    cp -R /build/eric-sc-logfwdr/gopath/src/github.com /build/eric-sc-logfwdr/gen/

WORKDIR /build/eric-sc-logfwdr/gen/github.com/envoyproxy/go-control-plane/envoy
RUN     go mod init github.com/envoyproxy/go-control-plane/envoy &&\
        cp -R /build/eric-sc-logfwdr/gopath/src/ericsson.com/ /build/eric-sc-logfwdr/gen/

WORKDIR /build/eric-sc-logfwdr/gen/ericsson.com/5g/scp/api
RUN     go mod init ericsson.com/5g/scp/api &&\
        cp -R /build/eric-sc-logfwdr/gopath/src/ericsson.com/ /build/eric-sc-logfwdr/gen/

WORKDIR /build/eric-sc-logfwdr/gen/ericsson.com/5g/scp/service
RUN     go mod init ericsson.com/5g/scp/service &&\
        echo 'Compiling ...' &&\
        sed -i '/udpa/d' /build/eric-sc-logfwdr/gen/github.com/envoyproxy/go-control-plane/envoy/data/cluster/v3/outlier_detection_event.pb.go &&\
        sed -i '/udpa/d' /build/eric-sc-logfwdr/gen/github.com/envoyproxy/go-control-plane/envoy/data/core/v3/health_check_event.pb.go

WORKDIR /logfwdr
RUN     go build -o logforwarder

FROM $CBOS_IMG as final
#Hardening script is not needed here as no packages are installed
COPY --from=builder /logfwdr/logforwarder /

# UID calculated with the input "eric-sc-logfwdr"; using the hashing function provided by ADP Design Rule 122:
# https://eteamspace.internal.ericsson.com/display/AA/Kubernetes+Security+Design+Rules#KubernetesSecurityDesignRules-DR-D1123-122
RUN echo "202932:x:202932:202932:An Identify for eric-sc-logfwdr:/nonexistent:/bin/false" >>/etc/passwd &&\
    echo "202932:!::0:::::" >>/etc/shadow &&\
    chown -R 202932:0 /logforwarder && chmod -R g=u /logforwarder &&\
    chown -R 202932:0 /mnt && chmod -R g=u /mnt

USER 202932
ENTRYPOINT ["/logforwarder"]
