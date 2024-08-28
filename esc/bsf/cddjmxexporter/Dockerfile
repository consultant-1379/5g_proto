ARG DOCKER_BASE_IMG
FROM $DOCKER_BASE_IMG

RUN mkdir -p /opt/jmx_exporter/
WORKDIR /opt/jmx_exporter/

EXPOSE 5556

COPY config.yaml /opt/jmx_exporter/config.yml
COPY start.sh /opt/jmx_exporter/

# properties file for logging
COPY logging.properties /opt/jmx_exporter/logging.properties

COPY target/libs /java-exec/libs
COPY target/classes /java-exec/classes

RUN echo "193650:x:193650:193650:An Identify for non-rootroot-cont:/nonexistent:/bin/false" >>/etc/passwd &&\
    echo "193650:!::0:::::" >>/etc/shadow && chown -R 193650:0 /opt/jmx_exporter/ && chmod -R g=u /opt/jmx_exporter/
USER 193650

ENTRYPOINT ["/opt/jmx_exporter/start.sh"]
