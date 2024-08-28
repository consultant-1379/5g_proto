FROM armdockerhub.rnd.ericsson.se/balabit/syslog-ng:4.1.1

CMD ["--no-caps"]

RUN apt-get update && apt-get install -y apt-transport-https && apt-get install -y vim && apt-get install -y procps && apt-get install -y openssl

#ADD certs /certs

#RUN 	cd /certs 													    \
# 	&&  mkdir -p /usr/local/etc/syslog-ng/cert.d 	 					\
#    &&  mkdir -p /usr/local/etc/syslog-ng/ca.d 							\
#    &&  mv syslog-key.pem /usr/local/etc/syslog-ng/cert.d/serverkey.pem \ 
#    &&  mv syslog.pem /usr/local/etc/syslog-ng/cert.d/servercert.pem    \
#    &&  cd /usr/local/etc/syslog-ng/ca.d								\
#    &&  hash_key=$(openssl x509 -noout -hash -in /certs/rootCA.crt) 	\
#    &&  mv /certs/rootCA.crt cacert.pem         						\
#	&&  ln -s cacert.pem $hash_key.0

#ADD syslog-ng.conf /etc/syslog-ng/

EXPOSE 6514