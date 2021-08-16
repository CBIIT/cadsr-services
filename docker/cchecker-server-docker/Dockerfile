FROM ncidockerhub.nci.nih.gov/cbiit/centos7_base
MAINTAINER Shaun Einolf

RUN yum -y update && yum -y install cronie git java-1.8.0-openjdk-devel sudo util-linux wget epel-release

WORKDIR /tmp
RUN wget https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.52/bin/apache-tomcat-9.0.52.tar.gz \
    && mkdir apache-tomcat && tar xvfz apache-tomcat-9.0.52.tar.gz -C ./apache-tomcat --strip-components 1 \
    && cd apache-tomcat/webapps && rm -rf examples docs host-manager manager ROOT/*  && cd ../.. \
    && mv apache-tomcat /usr/local/apache-tomcat

RUN wget https://archive.apache.org/dist/maven/maven-3/3.6.0/binaries/apache-maven-3.6.0-bin.tar.gz \
    && tar xvfz apache-maven-3.6.0-bin.tar.gz \
    && mv apache-maven-3.6.0 /opt \
    && ln -s /opt/apache-maven-3.6.0 /opt/apache-maven

ENV PATH=/opt/apache-maven/bin:$PATH
ENV JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk

WORKDIR /var/local
RUN mkdir -p /local/content/cchecker /logs

COPY entrypoint/run.sh /usr/bin/
COPY assets/crondelete /etc/cron.d/crondelete
COPY assets/web.xml /usr/local/apache-tomcat/conf
COPY assets/error.html /usr/local/apache-tomcat/webapps/ROOT
COPY assets/index.html /logs

RUN chmod 0644 /etc/cron.d/crondelete
RUN crontab /etc/cron.d/crondelete

EXPOSE 8080
ENTRYPOINT "/usr/bin/run.sh"
