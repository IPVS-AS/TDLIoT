# Topic Description Language for the Internet of Things (TDLIoT) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

The TDLIoT [[1]](#references) provides means to describe and find topics of sensors and actuators by 
 
 (i) a holistic description of the topics,  
 (ii) a topic catalog to browse the topic descriptions, and  
 (iii) an effective way to find suitable topics that offer access to sensors and actuators. 
 
In this way, IoT application development can be eased through an abstraction from specific IoT middlewares. 
The topics can be found, for example, based on a specific location, sensor or actuator type, data types, or data units. 

This repository contains a prototypical implementation to manage a number of topics (stored as Topic Description Language documents) in the context of IoT.
The project is subdivided into the following subprojects:  

* [Topic catalog](topic-catalogue): a data storage for topics described using the TDLIoT notation, and an API to publish, update and search for topics descriptions.
* [TDLWeb](TDLWeb): a web interface to execute the operations provided by the topic catalog.
* [TDLClient](TDLClient): a client java library to communicate to the topic catalog and execute provided operations. 

## Installing the Topic Catalog and the Web Interface on Linux

1. In the [```script.js```](TDLWeb/WebContent/lib/js/script.js) file, replace the IP address of the topic catalog by the IP address of the host, in which you are installing the topic catalog.  
2. Execute the install script [install.sh](install.sh)
3. Access the web interface available under  [http://yourHost:80](http://yourHost:80). 
The topic catalog is available under [http://yourHost:8080/swagger-ui.html](http://yourHost:8080/swagger-ui.html)

## Topic Catalog
In order to manage a lot of different topics, the topic catalog provides the topics as Topic Description Language (TDL)- documents. The catalog provides different operations, such as add, delete, update and search. Using a REST interface, all operations can be executed. 
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
TDLWeb provides a web interface to execute the provided operations of the topic catalog. Topic descriptions can be added, removed and edited.
![picture alt](https://raw.githubusercontent.com/IPVS-AS/TDLIoT/master/TDLWeb/screenshot.png)
Before using the web interface, please replace the link to the catalogue in the ```script.js``` file.

## TDLClient
The TDLClient is a Java library for using the topic catalog in your project. 
The TDLCLient contains two parts which can be used independently: 


The main *TDLClient* class provides functionality to publish and subscribe to topics with the MQTT protocol. 
Instead of extracting the address or topic manually out of the topic description, the client wraps all this functionality. 
The class provides an interface for subscribing to a single topic, multiple topics or all topics returned by a search. 
For publishing, the topic of the MQTT broker can be added in the same manner and returns the human readable topic name.
Internally it creates connections to the brokers and manages sending and receiving messages using the [eclipse paho](https://www.eclipse.org/paho/) MQTT client.


It relies on the *TDLUtil* class for accessing the topic catalog. 
It provides all REST interface possibilities in Java. 
Below, the interactions between the user and the classes are shown with an example:
The process of subscribing to a topic is shown with the internal resolution to the address of the broker. 
![TDLClient example usage](https://raw.githubusercontent.com/IPVS-AS/TDLIoT/master/TDLClient-example.png)

## References

1. Franco da Silva, Ana Cristina; Hirmer, Pascal; Breitenbücher, Uwe; Kopp, Oliver; Mitschang, Bernhard: [TDLIoT: A Topic Description Language for the Internet of Things](https://link.springer.com/chapter/10.1007/978-3-319-91662-0_27), In: Proceedings of the 18th International Conference on Web Engineering (ICWE), 2018.

## Haftungsausschluss

Dies ist ein Forschungsprototyp und enthält insbesondere Beiträge von Studenten.
Diese Software enthält möglicherweise Fehler und funktioniert möglicherweise, insbesondere bei variierten oder neuen Anwendungsfällen, nicht richtig.
Insbesondere beim Produktiveinsatz muss 1. die Funktionsfähigkeit geprüft und 2. die Einhaltung sämtlicher Lizenzen geprüft werden.
Die Haftung für entgangenen Gewinn, Produktionsausfall, Betriebsunterbrechung, entgangene Nutzungen, Verlust von Daten und Informationen, Finanzierungsaufwendungen sowie sonstige Vermögens- und Folgeschäden ist, außer in Fällen von grober Fahrlässigkeit, Vorsatz und Personenschäden ausgeschlossen.

## Disclaimer of Warranty

Unless required by applicable law or agreed to in writing, Licensor provides the Work (and each Contributor
provides its Contributions) on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
or implied, including, without limitation, any warranties or conditions of TITLE, NON-INFRINGEMENT,
MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE. You are solely responsible for determining the
appropriateness of using or redistributing the Work and assume any risks associated with Your exercise of
permissions under this License.