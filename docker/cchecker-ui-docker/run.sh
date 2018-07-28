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
npm install
ng build --output-path /var/www/localhost/htdocs 
cp /etc/apache2/conf.d/.htaccess /var/www/localhost/htdocs
exec httpd -D FOREGROUND
