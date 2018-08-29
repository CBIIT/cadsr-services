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
ng build --output-path /var/www/html 
cp /etc/httpd/conf.d/.htaccess /var/www/html
exec httpd -D FOREGROUND
