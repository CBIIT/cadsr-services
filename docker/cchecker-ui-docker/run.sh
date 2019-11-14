#!/bin/sh
echo "get current date for footer"
timestamp=`date`
if [ -d "/root/cadsr-services" ]; then
        echo "Skipping cc ui install"
  cd /root/cadsr-services
  git pull
else
  cd /root
  git clone https://github.com/CBIIT/cadsr-services
  branchOrTag="${BRANCH_OR_TAG/origin\//}"
  cd cadsr-services
  git checkout $branchOrTag
fi

echo $BRANCH_OR_TAG
version=`xmllint --xpath "//*[local-name()='project']/*[local-name()='version']/text()" /root/cadsr-services/cchecker-gateway/pom.xml`
cd /root/cadsr-services/cchecker-ui
echo "installing node modules"
npm install

if [ $configuration ]; then 
  sed "s#REPLACEME#$REST_API#g" -i src/environments/environment.${configuration}.ts
  sed "s#VERSIONNUMBER#$version#g" -i src/environments/environment.${configuration}.ts  
  sed "s/timestamp:.*/timestamp: '$timestamp',/g" -i src/environments/environment.${configuration}.ts  
  ng build -c=$configuration --output-path /var/www/html
else
  sed "s#VERSIONNUMBER#$version#g" -i src/environments/environment.${configuration}.ts
  sed "s/timestamp:.*/timestamp: '$timestamp',/g" -i src/environments/environment.ts
  ng build --output-path /var/www/html
fi

cp /etc/httpd/conf.d/.htaccess /var/www/html

# remote apache pid to prevent apache from failing to start #
rm /var/run/httpd/httpd.pid
exec httpd -D FOREGROUND
