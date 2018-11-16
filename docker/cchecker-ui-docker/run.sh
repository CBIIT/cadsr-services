#!/bin/sh
if [ -d "/root/cadsr-services" ]; then
        echo "Skipping cc ui install"
  cd /root/cadsr-services
  git pull
else
  cd /root
  git clone https://github.com/CBIIT/cadsr-services
fi
cd /root/cadsr-services/cchecker-ui
echo "installing node modules"
npm install

if [ $configuration ]; then 
  sed "s#REPLACEME#$apiUrl#g" -i src/environments/environment.${configuration}.ts
  ng build -c=$configuration --output-path /var/www/html
else
  ng build --output-path /var/www/html
fi

cp /etc/httpd/conf.d/.htaccess /var/www/html

# remote apache pid to prevent apache from failing to start #
rm /var/run/httpd/httpd.pid
exec httpd -D FOREGROUND
