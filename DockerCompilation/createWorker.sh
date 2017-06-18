#!/usr/bin/env bash

SWARMTOKEN="SWMTKN-1-0gbmr3a7x6dvmv156ty0o0u5wiyrq1vwquh42lr21ggt2v9tpf-bc683dfr6ajk4b5oeozta5jxm"
MANAGERIP="141.37.160.143"
MANAGERPORT="2377"

# worker1 löschen
docker-machine rm -f worker

# neuen worker erstellen
docker-machine create --driver virtualbox worker

# Portforwarding aktivieren
VBoxManage controlvm "worker" natpf1 "tcp-port27020,tcp,,27020,,27020"
VBoxManage controlvm "worker" natpf1 "tcp-port27021,tcp,,27021,,27021"


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
echo "Mainserver images speichern und auf worker schieben"
echo "########################################################################"
docker save -o main.tar akka-http-microservice-mainserver:1.0
docker-machine scp main.tar worker:.
# in Manager einloggen und Images laden
docker-machine ssh worker docker load < main.tar
# in Manager einloggen und Images löschen
docker-machine ssh worker rm -rf main.tar


echo "########################################################################"
echo "Workerserver images speichern und auf worker schieben"
echo "########################################################################"
docker save -o worker.tar akka-http-microservice-workerserver:1.0
docker-machine scp worker.tar worker:.
# in Manager einloggen Images laden
docker-machine ssh worker docker load<worker.tar
# in Manager einloggen und Images löschen
docker-machine ssh worker rm -rf worker.tar

echo "########################################################################"
echo "Actorsystem images speichern und auf worker schieben"
echo "########################################################################"
docker save -o actor.tar actorsystem:1.0
docker-machine scp actor.tar worker:.
# in Manager einloggen Images laden
docker-machine ssh worker docker load<actor.tar
# in Manager einloggen und Images löschen
docker-machine ssh worker rm -rf actor.tar

docker swarm join --token $SWARMTOKEN $MANAGERIP:$MANAGERPORT
