FROM alpine:latest

ARG VERSION

ENV FLING_API_BASE http://localhost:3000
ENV FLING_LOG_LEVEL warn
ENV FLING_FILESIZE 209715200

RUN apk add --update --no-cache nginx openjdk11-jre gettext && \
  mkdir -p /var/fling/files && \
  mkdir -p /tmp/fling && \
  wget -O /tmp/fling/service.jar "https://nexus.friedl.net/service/rest/v1/search/assets/download?sort=version&maven.groupId=net.friedl&maven.artifactId=fling&maven.baseVersion=$(echo -n $VERSION | tr [:lower:] [:upper:])&maven.extension=jar" && \
  wget -O /tmp/fling/web.tar.gz "https://nexus.friedl.net/repository/build-artifacts/fling-web-$(echo -n $VERSION | tr [:upper:] [:lower:]).tar.gz" && \
  tar xzf /tmp/fling/web.tar.gz -C /tmp/fling && \
  ls -al /tmp/fling && \
  mkdir -p /var/www/fling && mv /tmp/fling/build/* /var/www/fling && \
  mkdir -p /var/fling && mkdir -p /var/fling/files && \
  mv /tmp/fling/service.jar ./service.jar

COPY ./etc/nginx/conf.d /etc/nginx/conf.d
COPY ./var/www/fling/config.js.template /var/www/fling/config.js.template
COPY ./entrypoint.sh /usr/local/bin/entrypoint.sh

VOLUME /var/fling/

EXPOSE 3000

ENTRYPOINT ["entrypoint.sh"]
