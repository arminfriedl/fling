FROM debian:stable

RUN ls -al

RUN apt-get update -y && apt-get upgrade -y && \
  apt-get install -y nginx openjdk-11-jre && \
  mkdir -p /var/www/fling && \
  mv web/fling/build/* /var/www/fling/ && \
  rm -fr web \

COPY ./etc/nginx/conf.d /etc/nginx/conf.d

ENTRYPOINT ["/bin/sh"]
