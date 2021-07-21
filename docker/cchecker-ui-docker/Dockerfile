FROM ncidockerhub.nci.nih.gov/cbiit/centos7_base
MAINTAINER Shaun Einolf
ENV VERSION=v14.17.3
ENV DISTRO=linux-x64
ENV NODEJS_VERSION=v14.17.3
ENV PATH=/usr/local/lib/nodejs/node-$VERSION-$DISTRO/bin:$PATH

RUN mkdir -p /usr/local/lib/nodejs
RUN yum -y install epel-release
RUN yum -y update && yum -y install git httpd sudo util-linux libxml2 wget epel-release
RUN cd /etc/yum.repos.d && wget https://repo.codeit.guru/codeit.el`rpm -q --qf "%{VERSION}" $(rpm -q --whatprovides redhat-release)`.repo
RUN yum -y install httpd
WORKDIR /tmp
RUN wget https://nodejs.org/dist/$VERSION/node-$VERSION-$DISTRO.tar.xz
RUN tar -xJvf node-$VERSION-$DISTRO.tar.xz -C /usr/local/lib/nodejs 

RUN npm install -g @angular/cli \
    && git config --global user.email "you@example.com" \
    && git config --global user.name "Your Name"

WORKDIR /usr/lib/node_modules/npm
RUN npm install --save-dev https-proxy-agent@2.2.4 
RUN find / -name ssl-cert-snakeoil.key  -exec rm {} +
ENV abc=123

ADD conf/httpd.conf /etc/httpd/conf
ADD conf/.htaccess /etc/httpd/conf.d
ADD conf/autoindex.conf /etc/httpd/conf.d
ADD run.sh /usr/bin/
RUN chmod 777 /usr/bin/run.sh
RUN mkdir /run/apache2/
EXPOSE 80
ENTRYPOINT ["/usr/bin/run.sh"]
