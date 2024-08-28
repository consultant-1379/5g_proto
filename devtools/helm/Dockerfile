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
# Created on: Jun 03, 2018
#     Author: eedsvs
#

FROM centos

COPY plugins/release/plugin.yaml /tmp

RUN yum install -y git epel-release wget openssl curl which unzip			\
	&& curl https://storage.googleapis.com/kubernetes-helm/helm-v2.10.0-linux-amd64.tar.gz > /tmp/helm-v2.10.0-linux-amd64.tar.gz											 \
	&& tar -C /tmp -xvf /tmp/helm-v2.10.0-linux-amd64.tar.gz					\
	&& mv /tmp/linux-amd64/helm /usr/local/bin/								\
	&& rm -rf /tmp/linux-amd64/												\
    && helm init || true 													\
    && yum clean all 														\
    && rm -rf /var/cache/yum												\
    && mkdir -p /root/.helm/plugins/release 								\
    && mv /tmp/plugin.yaml /root/.helm/plugins/release 						\
    && cd /tmp 																\
    && wget https://github.com/sstarcher/helm-release/releases/download/0.2.0/helm-release_0.2.0_linux_amd64.tar.gz								\
    && tar -C /root/.helm/plugins/release -xvzf /tmp/helm-release_0.2.0_linux_amd64.tar.gz \
    && rm -rf /tmp/*
RUN export username=$(wget -O - -q 'http://localhost:1354?url=arm.rnd&username') \
    && export password=$(wget -O - -q 'http://localhost:1354?url=arm.rnd&password') \
    && helm repo add eric-cm-mediator https://arm.rnd.ki.sw.ericsson.se/artifactory/proj-adp-gs-all-helm/ --username $username --password $password \
    && helm repo add eric-data-document-database-pg https://arm.rnd.ki.sw.ericsson.se/artifactory/proj-adp-gs-all-helm/ --username $username --password $password \
    && helm repo add eric-data-wide-column-database-cd https://arm.rnd.ki.sw.ericsson.se/artifactory/proj-adp-gs-all-helm/ --username $username --password $password \
    && helm repo add eric-data-coordinator-zk https://arm.rnd.ki.sw.ericsson.se/artifactory/proj-adp-gs-all-helm/ --username $username --password $password \
    && helm repo add eric-data-message-bus-kf https://arm.rnd.ki.sw.ericsson.se/artifactory/proj-adp-gs-all-helm/ --username $username --password $password \
    && helm repo add eric-fh-alarm-handler https://arm.rnd.ki.sw.ericsson.se/artifactory/proj-adp-gs-all-helm/ --username $username --password $password \
    && helm repo add eric-pm-server https://arm.rnd.ki.sw.ericsson.se/artifactory/proj-adp-gs-all-helm/ --username $username --password $password 

RUN export username=$(wget -O - -q 'http://localhost:1354?url=armdocker&username') \
    && export password=$(wget -O - -q 'http://localhost:1354?url=armdocker&password') \
    && helm repo add eric-bsf https://armdocker.rnd.ericsson.se/artifactory/proj-5g-bsf-helm/ --username $username --password $password \
    && helm repo update 
WORKDIR /build/

