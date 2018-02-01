# TDLIoT
This repository provides utils to manage a number of topics (stored as Topic Description Language documents) in the context of IoT.
The project is subdivided into the following subprojects:
* topic-catalogue
* TDLWeb
* TDLClient
## Topic catalogue
In order to manage a lot of different topics, the topic catalogue provides the topics as Topic Description Language (TDL)- documents. The catalogue provides different operations, such as add, delete, update and search. Using a REST interface, all operations can be executed. 
The REST interface is documented using swagger and is available under [http://yourHost:8080/swagger-ui.html](http://yourHost:8080/swagger-ui.html)
The tdl-catalogue is also accessible as docker image and hosted on [Docker Hub](https://hub.docker.com/r/ipvs/tdl-catalogue/)
### Installation 
([docker](https://www.docker.com/) and [docker-compose](https://docs.docker.com/compose/) is required)
1. Switch into the directory :
```cd tdl-catalogue```
2. Build the docker image:
```docker build -t ipvs/tdl-catalogue .```
3. Run the docker compose file (The docker-compose file starts the tdl-catalogue and a mongoDB instance):
```docker-compose up```
## TDLWeb
TDLWeb provides a web interface to execute the provided operations of the tdl-catalogue. tdl- entries can be added, removed and edited.
![picture alt](https://raw.githubusercontent.com/IPVS-AS/TDLIoT/master/TDLWeb/screenshot.png)
Before using the web interface, please replace the link to the catalogue in the ```script.js``` file.
## TDLClient
The TDLClient provides a library, which automatically creates stubs based on the derived TDL's. The TDL's

