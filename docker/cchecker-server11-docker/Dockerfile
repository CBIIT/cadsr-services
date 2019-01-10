FROM cbiit/centos7_base
MAINTAINER Shaun Einolf

RUN yum -y update && yum -y install cronie git java-11-openjdk-devel sudo util-linux wget which
WORKDIR /tmp
RUN wget https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.11/bin/apache-tomcat-9.0.11.tar.gz \
    && tar xvfz apache-tomcat-9.0.11.tar.gz \
    && mv apache-tomcat-9.0.11 /usr/local/apache-tomcat

RUN wget https://archive.apache.org/dist/maven/maven-3/3.5.4/binaries/apache-maven-3.5.4-bin.tar.gz \
    && tar xvfz apache-maven-3.5.4-bin.tar.gz \
    && mv apache-maven-3.5.4 /opt \
    && ln -s /opt/apache-maven-3.5.4 /opt/apache-maven

ENV PATH=/opt/apache-maven/bin:$PATH
ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk

WORKDIR /var/local
RUN mkdir -p /local/content/cchecker /logs

COPY entrypoint/run.sh /usr/bin/
COPY crondelete /etc/cron.d/crondelete
RUN chmod 0644 /etc/cron.d/crondelete
RUN crontab /etc/cron.d/crondelete

EXPOSE 8080 8082 
ENTRYPOINT "/usr/bin/run.sh"
