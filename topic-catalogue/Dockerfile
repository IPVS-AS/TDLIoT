FROM java:8-jre-alpine
MAINTAINER ipvs.uni-stuttgart.de
WORKDIR /usr/src/app
COPY build/libs/topic-catalogue-0.0.1-SNAPSHOT.jar ./app.jar
EXPOSE 8080
CMD java -Dmongodb.ip=${MONGODB_IP} -Dmongodb.port:${MONGODB_PORT} -jar ./app.jar
