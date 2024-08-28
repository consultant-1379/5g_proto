ARG DOCKER_BASE_IMG
FROM $DOCKER_BASE_IMG

EXPOSE 6379
RUN mkdir -p /redis_conf
COPY ./redis.conf /redis_conf
CMD [ "redis-server" , "/redis_conf/redis.conf" ]
