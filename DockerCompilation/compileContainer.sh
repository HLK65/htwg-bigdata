#!/bin/sh
MYMAIN="test"

docker stop $(docker ps -a -q)
docker rm $(docker ps -a -q)

# Alle Images löschen
docker rmi -f akka-http-microservice-mainserver:1.0
docker rmi -f akka-http-microservice-workerserver:1.0
docker rmi -f actorsystem:1.0

# Mainserver compilieren und starten
cd ../MainServer
sbt docker:publishLocal
docker run -p 27020:27020 -d=true --name "main" akka-http-microservice-mainserver:1.0

# Workerserver compilieren und starten
cd ../WorkerServer
sbt docker:publishLocal
docker run -p 27021:27021 -d=true --name "worker" akka-http-microservice-workerserver:1.0

# Actorsystem compilieren und starten
cd ../ActorSystem
sbt docker:publishLocal
docker run -d=true --name "actor" actorsystem:1.0

sh ../TerminalGui/ant_gui.sh 192.168.99.100:27020