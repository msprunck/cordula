FROM java:openjdk-8-jre-alpine
MAINTAINER Matthieu Sprunck <matthieu@sprunck.com>

WORKDIR /

COPY docker/cordula/docker-entrypoint.sh \
     docker/cordula/start.sh \
     docker/cordula/log4j.properties \
     target/cordula.jar /

EXPOSE 3000
ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["cordula"]