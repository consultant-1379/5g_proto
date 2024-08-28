FROM armdockerhub.rnd.ericsson.se/golang

ARG app_env
ENV APP_ENV $app_env

COPY go/bin/helloKube /go/bin
WORKDIR /go/bin

CMD ./helloKube
