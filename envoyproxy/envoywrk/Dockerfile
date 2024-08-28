ARG DOCKER_BASE_IMG
FROM $DOCKER_BASE_IMG

RUN mkdir -p /worker &&\
    chown -R 203342:0 /worker &&\
    chmod -R g=u /worker

COPY worker_shutdown.sh /scripts/worker_shutdown.sh
COPY entrypoint.sh /scripts/entrypoint.sh

WORKDIR /worker

# UID calculated with the input "envoywrk"; using the hashing function provided by ADP Design Rule 122:
# https://eteamspace.internal.ericsson.com/display/AA/Kubernetes+Security+Design+Rules#KubernetesSecurityDesignRules-DR-D1123-122
RUN echo "203342:x:203342:203342:An Identify for envoywrk:/nonexistent:/bin/false" >> /etc/passwd && \
    echo "203342:!::0:::::" >>/etc/shadow && \
    chown 203342:0 /usr/local/bin/envoy && chmod g=u /usr/local/bin/envoy && \
    chown 203342:0 /usr/local/bin/VERSION_INFO && chmod g=u /usr/local/bin/VERSION_INFO && \
    chown -R 203342:0 /scripts && chmod -R g=u /scripts && \
    chmod +x /scripts/entrypoint.sh && chmod +x /scripts/worker_shutdown.sh && \
    chown -R 203342:0 /mnt && chmod -R g=u /mnt
USER 203342

ENTRYPOINT ["/scripts/entrypoint.sh"]
