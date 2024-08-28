################################################################################
#
#  Author   : eustone
#
#  Revision : 1.0
#  Date     : 2024-06-10 18:20:00
#
################################################################################
#
# (C) COPYRIGHT ERICSSON GMBH 2024
#
# The copyright to the computer program(s) herein is the property of
# Ericsson GmbH, Germany.
#
# The program(s) may be used and/or copied only with the written permission
# of Ericsson GmbH in accordance with the terms and conditions stipulated in
# the agreement/contract under which the program(s) have been supplied.
#
################################################################################

mkdir -p ~/cert-tmp && cd ~/cert-tmp
sudo install -C -m 640 -o eccd -g eccd /etc/kubernetes/pki/ca.crt ca.crt;
sudo install -C -m 640 -o eccd -g eccd /etc/kubernetes/pki/ca.key ca.key;
openssl genrsa -out registry.key 4096
cat /etc/ssl/openssl.cnf <(printf "\n[SAN]\nsubjectAltName=DNS:registry.eccd.local") > csr.conf
openssl req -new -sha256 -key registry.key -subj "/CN=registry.eccd.local" -reqexts SAN -config csr.conf -out new-cert.csr
openssl x509 -req -in new-cert.csr -CA ca.crt -CAkey ca.key -CAcreateserial -out registry.crt -days 3650 -sha256 -extensions SAN -extfile csr.conf
sudo install -C -m 400 -o root -g root registry.crt /etc/docker-distribution/registry/registry.crt
sudo install -C -m 400 -o root -g root registry.key /etc/docker-distribution/registry/registry.key
sudo install -C -m 400 -o root -g root registry.crt /etc/docker-distribution/registry/registry.crt
sudo service docker-distribution restart
