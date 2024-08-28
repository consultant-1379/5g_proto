# vim:ts=4:sw=4:sw=4:et
#
# COPYRIGHT ERICSSON GMBH 2018
#
# The copyright to the computer program(s) herein is the property
# of Ericsson GmbH, Germany.
#
# The program(s) may be used and/or copied only with the written
# permission of Ericsson GmbH in accordance with
# the terms and conditions stipulated in the agreement/contract
# under which the program(s) have been supplied.
#
# Created on: Dec 04, 2018
#     Author: ehsacah
#

FROM armdockerhub.rnd.ericsson.se/golang:1.16.4-alpine AS builder
RUN apk --no-cache add --virtual .build-deps git make build-base

ARG K6_VERSION
RUN git clone https://github.com/k6io/k6
WORKDIR $GOPATH/k6
RUN git checkout tags/$K6_VERSION

RUN go get . && CGO_ENABLED=0 go install -a -ldflags '-s -w'

FROM armdockerhub.rnd.ericsson.se/alpine:3.9
RUN apk add --update \
    curl \
    procps \
    && rm -rf /var/cache/apk/*
COPY --from=builder /go/bin/k6 /usr/bin/k6
ADD src /tests
ADD certs /certs
WORKDIR /tests

#keeps the container running
CMD tail -f /dev/null

