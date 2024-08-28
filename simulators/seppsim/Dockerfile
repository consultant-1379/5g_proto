ARG DOCKER_BASE_IMG
FROM $DOCKER_BASE_IMG
 
COPY target/docker/entrypoint.sh /java-exec/entrypoint.sh
COPY target/libs /java-exec/libs
COPY target/classes /java-exec/classes

ENTRYPOINT ["/bin/bash", "-c", "/java-exec/entrypoint.sh java -cp /java-exec/libs/*:/java-exec/classes ${JVM_PARAMS} com.ericsson.sim.sepp.r17.SeppSimulator"]
