FROM golang

ARG app_env
ENV APP_ENV $app_env

COPY go/bin/helloADP /go/bin
WORKDIR /go/bin

CMD ./helloADP
