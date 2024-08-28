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
#     Author: eedsvs
#

FROM ubuntu

ADD builddir /root

COPY README.txt /root

RUN apt-get update
RUN apt-get install --yes curl
RUN apt-get install --yes make
RUN apt-get install --yes lsb-release
RUN apt-get install --yes sudo
RUN apt-get install --yes git
RUN	cd /root/devtools/h2load; /usr/bin/make install-ubuntu

ENTRYPOINT ["/bin/sh"]

# ENTRYPOINT ["/usr/local/bin/h2load"]
