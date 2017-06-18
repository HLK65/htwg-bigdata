#!/usr/bin/env bash
MANAGERIP="192.168.99.103"

# manager1 löschen
docker-machine rm -f manager1

# neuen manager erstellen
docker-machine create --driver virtualbox manager1
MANAGERIP=$(docker-machine ip manager1)
echo "########################################################################"
echo $MANAGERIP
echo "########################################################################"

# Portforwarding aktivieren
VBoxManage controlvm "manager1" natpf1 "tcp-port27020,tcp,,27020,,27020"
VBoxManage controlvm "manager1" natpf1 "tcp-port27021,tcp,,27021,,27021"
VBoxManage controlvm "manager1" natpf1 "tcp-port2377,tcp,,2377,,2377"


echo "########################################################################"
echo "images bauen"
echo "########################################################################"
cd ../MainServer
sbt docker:publishLocal
cd ../WorkerServer
sbt docker:publishLocal
cd ../ActorSystem
sbt docker:publishLocal

echo "########################################################################"
echo "Mainserver images speichern und auf manager schieben"
echo "########################################################################"
docker save -o main.tar akka-http-microservice-mainserver:1.0
docker-machine scp main.tar manager1:.
# in Manager einloggen und Images laden
docker-machine ssh manager1 docker load < main.tar
# in Manager einloggen und Images löschen
docker-machine ssh manager1 rm -rf main.tar


echo "########################################################################"
echo "Workerserver images speichern und auf manager schieben"
echo "########################################################################"
docker save -o worker.tar akka-http-microservice-workerserver:1.0
docker-machine scp worker.tar manager1:.
# in Manager einloggen Images laden
docker-machine ssh manager1 docker load<worker.tar
# in Manager einloggen und Images löschen
docker-machine ssh manager1 rm -rf worker.tar

echo "########################################################################"
echo "Actorsystem images speichern und auf manager schieben"
echo "########################################################################"
docker save -o actor.tar actorsystem:1.0
docker-machine scp actor.tar manager1:.
# in Manager einloggen Images laden
docker-machine ssh manager1 docker load<actor.tar
# in Manager einloggen und Images löschen
docker-machine ssh manager1 rm -rf actor.tar

echo "########################################################################"
echo "Swarm erstellen"
echo "########################################################################"
docker-machine ssh manager1 docker swarm init --advertise-addr $MANAGERIP
# Registry TODO ?!

# Mainserver-Service erstellen
docker-machine ssh manager1 docker service create -p 27020:27020 --name mainservice akka-http-microservice-mainserver:1.0
# Workerserver-Service erstellen
docker-machine ssh manager1 docker service create -p 27021:27021 --name workerservice akka-http-microservice-workerserver:1.0
# Actorsystem-Service erstellen
sleep 2
docker-machine ssh manager1 docker service create --name actorservice actorsystem:1.0

sh ../TerminalGui/ant_gui.sh $MANAGERIP:27020
