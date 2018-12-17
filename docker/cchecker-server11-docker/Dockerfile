FROM cbiit/centos7_base
MAINTAINER Shaun Einolf

RUN yum -y update && yum -y install cronie git sudo util-linux wget
WORKDIR /tmp
RUN wget https://download.java.net/openjdk/jdk11/ri/openjdk-11+28_linux-x64_bin.tar.gz \
    && tar xvfz openjdk-11+28_linux-x64_bin.tar.gz \
    && mv jdk-11 /usr/lib/jvm

RUN wget https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.11/bin/apache-tomcat-9.0.11.tar.gz \
    && tar xvfz apache-tomcat-9.0.11.tar.gz \
    && mv apache-tomcat-9.0.11 /usr/local/apache-tomcat

RUN wget https://archive.apache.org/dist/maven/maven-3/3.5.4/binaries/apache-maven-3.5.4-bin.tar.gz \
    && tar xvfz apache-maven-3.5.4-bin.tar.gz \
    && mv apache-maven-3.5.4 /opt \
    && ln -s /opt/apache-maven-3.5.4 /opt/apache-maven

ENV JAVA_HOME=/usr/lib/jvm/jdk-11
ENV PATH=/opt/apache-maven/bin:$JAVA_HOME/bin:$PATH

WORKDIR /var/local
RUN mkdir -p /local/content/cchecker

COPY entrypoint/run.sh /usr/bin/
COPY crondelete /etc/cron.d/crondelete
RUN chmod 0644 /etc/cron.d/crondelete
RUN crontab /etc/cron.d/crondelete

EXPOSE 1551 8080 4801
ENTRYPOINT "/usr/bin/run.sh"
