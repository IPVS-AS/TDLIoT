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
The TDLClient is a Java library for using the topic catalogue in your project. 
The contains two parts which can be used independetly: 


The main *TDLClient* class provides functionality to publish and subscribe to topics with the MQTT protocol. 
Instead of extracting the address or topic manually out of the topic description, the client wraps all this functionality. 
The class provides an interface for subscribing to a single topic tdl, multiple topics or all topics returned by a search. 
For publishing the TDL of the broker can be added in the same manner and returns the human readable topic name.
Internally it creates connections to the brokers and manages the send and incoming messages using the [eclipse paho](https://www.eclipse.org/paho/) MQTT client.


It relies on the *TDLUtil* class for accessing the topic catalogue. 
It provides all REST interface possibilities in Java. 
Below the interactions between the user and the classes are shown with an example:
There the process of subscribing to a topic is shown with the internal resolution to the address of the broker. 
![TDLClient example usage](https://raw.githubusercontent.com/IPVS-AS/TDLIoT/master/TDLClient-example.png)

