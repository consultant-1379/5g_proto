ARG INFLUXDB_VERSION
FROM armdockerhub.rnd.ericsson.se/influxdb:$INFLUXDB_VERSION

EXPOSE 8086

# deprecated dockerfile change
# replaced by configmap
#COPY influxdb.conf /etc/influxdb/influxdb.conf

# deprecated dockerfile certificates
# replaced by external certificate CRs

# influxdb private key 5g_proto/scripts/certificates/certm_worker/keys/influxdb/key.pem
#COPY my-key.pem /etc/ssl/key.pem

# influxdb certificate 5g_proto/scripts/certificates/certm_worker/keys/influxdb/cert.pem
#COPY my-certs.pem /etc/ssl/certificate.pem

# rootca certificate 5g_proto/scripts/certificates/certm_worker/keys/rootca/rootCA.crt
#COPY my-ca.pem /etc/ssl/certs/ca-certs-SC.pem
