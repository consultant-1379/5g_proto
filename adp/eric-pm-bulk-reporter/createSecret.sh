#!/bin/bash

#Generate MD5 hash for "rootroot" password and print it
echo "rootroot" | openssl passwd -1 -stdin
#Create secret
k create secret generic pm-br-sftp-users-secret --from-file=./users.yaml
