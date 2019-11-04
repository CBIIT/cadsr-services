FROM ncidockerhub.nci.nih.gov/cbiit/centos7_base
MAINTAINER Shaun Einolf
RUN yum -y install epel-release
RUN yum -y update && yum -y install git httpd sudo util-linux libxml2
RUN curl --silent --location https://rpm.nodesource.com/setup_10.x | sudo bash - \
    && yum -y install nodejs

RUN npm install -g @angular/cli \
    && git config --global user.email "you@example.com" \ 
    && git config --global user.name "Your Name" 
WORKDIR /usr/lib/node_modules/npm
RUN npm install --save-dev https-proxy-agent@2.2.4 
RUN find / -name ssl-cert-snakeoil.key  -exec rm {} +
ENV abc=123
ADD conf/httpd.conf /etc/httpd/conf
ADD conf/.htaccess /etc/httpd/conf.d
ADD run.sh /usr/bin/
RUN chmod 777 /usr/bin/run.sh
RUN mkdir /run/apache2/
EXPOSE 80
ENTRYPOINT ["/usr/bin/run.sh"]
