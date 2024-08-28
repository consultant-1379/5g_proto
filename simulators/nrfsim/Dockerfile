ARG DOCKER_BASE_IMG
FROM $DOCKER_BASE_IMG

EXPOSE 80
EXPOSE 443
EXPOSE 8080

COPY target/docker/entrypoint.sh /java-exec/entrypoint.sh
COPY target/libs /java-exec/libs
COPY target/classes /java-exec/classes

ENTRYPOINT ["/bin/bash", "-c", "/java-exec/entrypoint.sh java -cp /java-exec/libs/*:/java-exec/classes ${JVM_PARAMS} com.ericsson.sim.nrf.r17.NrfSimulator"]
