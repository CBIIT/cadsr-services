FROM maven:3.5.4-jdk-8-alpine
RUN apk update \
    && apk add git \
    util-linux

WORKDIR /tmp
RUN wget http://www.gtlib.gatech.edu/pub/apache/tomcat/tomcat-9/v9.0.11/bin/apache-tomcat-9.0.11.tar.gz \
    && tar xvfz apache-tomcat-9.0.11.tar.gz \
    && mv apache-tomcat-9.0.11 /usr/local/apache-tomcat

WORKDIR /var/local
RUN mkdir -p /local/content/cchecker

COPY entrypoint/run.sh /usr/bin/

EXPOSE 1551 8080 4801
ENTRYPOINT "/usr/bin/run.sh"
