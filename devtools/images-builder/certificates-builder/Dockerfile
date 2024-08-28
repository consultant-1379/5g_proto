FROM armdocker.rnd.ericsson.se/sandbox/adp-staging/adp-cicd/bob-basebuilder.alpine:latest

RUN apk update && \
  apk add --no-cache \
  openssh-client \
  openssl \ 
  expect \
  openssh \
  jq \
  curl && \
  curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl" && \
  chmod a+x kubectl && \
  mv kubectl /bin/kubectl && \
  echo "StrictHostKeyChecking accept-new" >> /etc/ssh/ssh_config

CMD ["/bin/bash"]
