FROM golang

ARG app_env
ENV APP_ENV $app_env

COPY go/bin/helloCM /go/bin
WORKDIR /go/bin

CMD ./helloCM

