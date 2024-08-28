
ARG DOCKER_BASE_IMG
FROM $DOCKER_BASE_IMG

ARG SLES_REPO
ARG DOCKER_BASE_JAVA
ARG S3CLIENT_ID
RUN zypper ar -C -G -f $SLES_REPO?ssl_verify=no LDC-SLES15 && \
    zypper ref -f -r LDC-SLES15 && \
    zypper in -l -y --no-recommends $DOCKER_BASE_JAVA && \
    zypper clean -a

RUN mkdir -p /etc/s3client/java-exec

RUN echo "${S3CLIENT_ID}:x:${S3CLIENT_ID}:${S3CLIENT_ID}:An ${S3CLIENT_ID} entity for s3client:/nonexistent:/bin/false" >>/etc/passwd
RUN echo "${S3CLIENT_ID}:!::0:::::" >>/etc/shadow
RUN chown -R ${S3CLIENT_ID}:0 /etc/s3client/java-exec && \
    chmod -R g=u /etc/s3client/java-exec

COPY target/s3client.jar /etc/s3client/java-exec/s3client.jar

USER ${S3CLIENT_ID}
WORKDIR /etc/s3c
CMD java $ERIC_SC_CLIENT_JVM_PARAMETERS -jar /etc/s3client/java-exec/s3client.jar
