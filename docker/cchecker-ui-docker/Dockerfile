FROM cbiit/centos7_base
MAINTAINER Shaun Einolf
RUN yum -y install epel-release
RUN yum -y update && yum -y install git httpd sudo util-linux
RUN curl --silent --location https://rpm.nodesource.com/setup_10.x | sudo bash - \
    && yum -y install nodejs

RUN npm install -g @angular/cli \
    && git config --global user.email "you@example.com" \ 
    && git config --global user.name "Your Name" 

ADD conf/httpd.conf /etc/httpd/conf
ADD conf/.htaccess /etc/httpd/conf.d
ADD run.sh /usr/bin/
RUN chmod 777 /usr/bin/run.sh
RUN mkdir /run/apache2/
EXPOSE 80
ENTRYPOINT ["/usr/bin/run.sh"]
