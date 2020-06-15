FROM debian:stable

RUN apt-get update -y && apt-get upgrade -y && \
  apt-get install -y nginx openjdk-11-jre wget && \
  mkdir -p /var/fling/files && \
  mkdir -p /tmp/fling && \
  wget -O /tmp/fling/service.jar "https://nexus.friedl.net/service/rest/v1/search/assets/download?sort=version&maven.groupId=net.friedl&maven.artifactId=fling&maven.baseVersion=*SNAPSHOT&maven.extension=jar" && \
  wget -O /tmp/fling/web.tar.gz "https://nexus.friedl.net/repository/build-artifacts/fling-web-latest.tar.gz" && \
  tar xzf /tmp/fling/web.tar.gz -C /tmp/fling && \
  ls -al /tmp/fling && \
  mkdir -p /var/www/fling && mv /tmp/fling/build/* /var/www/fling && \
  mkdir -p /var/fling && mkdir -p /var/fling/files && \
  mv /tmp/fling/service.jar ./service.jar

COPY ./etc/nginx/conf.d /etc/nginx/conf.d
COPY ./entrypoint.sh ./usr/local/bin/entrypoint.sh

VOLUME /var/fling/

EXPOSE 3000

ENTRYPOINT ["entrypoint.sh"]