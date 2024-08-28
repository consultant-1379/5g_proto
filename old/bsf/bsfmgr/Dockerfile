FROM golang

ARG app_env
ENV APP_ENV $app_env

COPY go/bin/bsfmgr /go/bin
WORKDIR /go/bin

CMD ./bsfmgr

