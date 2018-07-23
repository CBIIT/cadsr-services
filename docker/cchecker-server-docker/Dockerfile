FROM maven:3.5.4-jdk-8-alpine
RUN apk update \
    && apk add git \
    util-linux

WORKDIR /tmp
RUN wget http://apache.mirrors.tds.net/tomcat/tomcat-9/v9.0.10/bin/apache-tomcat-9.0.10.tar.gz \
    && tar xvfz apache-tomcat-9.0.10.tar.gz \
    && mv apache-tomcat-9.0.10 /usr/local/apache-tomcat

WORKDIR /var/local
RUN git clone https://github.com/CBIIT/cadsr-services \
    && mkdir -p /local/content/cchecker

COPY entrypoint/run.sh /usr/bin/

EXPOSE 8080 4801
ENTRYPOINT "/usr/bin/run.sh"
