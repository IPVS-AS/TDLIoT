#!/bin/bash

#### installing docker
installed=$(dpkg -s docker-ce| grep installed)

if [ "" != "$installed" ]; then
 echo "Docker Engine already installed";
else
 echo "Installing Docker Engine";
 curl -sSL https://get.docker.com | sh
 sudo sed -ie "s@ExecStart=\/usr\/bin\/dockerd -H fd:\/\/@ExecStart=\/usr\/bin\/dockerd -H fd:\/\/ -H tcp:\/\/0.0.0.0:2375 -H unix:///var/run/docker.sock@g" /lib/systemd/system/docker.service
 echo 'DOCKER_OPTS="-H tcp://0.0.0.0:2375 -H unix:///var/run/docker.sock"' >> /etc/default/docker
 sudo systemctl daemon-reload
 sudo service docker restart
 sudo usermod -aG docker $USER
fi

#### installing docker-compose
sudo curl -L https://github.com/docker/compose/releases/download/1.22.0/docker-compose-$(uname -s)-$(uname -m) -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
docker-compose --version

#### installing Apache Server
sudo apt-get -qqy install apache2

#### installing TDL catalog
docker pull ipvs/tdl-catalogue
cd topic-catalogue
docker-compose up -d

cd ../TDLWeb/WebContent/
sudo cp -R * var/www/html 
