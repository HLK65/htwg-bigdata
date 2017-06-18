#!/usr/bin/env bash

SWARMTOKEN="SWMTKN-1-2votdth7srm7v86rk7roln1yhbx0cjdc6zxh7qnsafrmosrwgo-1lsf3xn81ithyocj360z5gi3t"
MANAGERIP="192.168.99.102"
MANAGERPORT="2377"

# worker löschen
docker-machine rm -f worker1

# neuen worker erstellen
docker-machine create --virtualbox-cpu-count "2" --virtualbox-memory "4096" --driver virtualbox worker1

# Portforwarding aktivieren
VBoxManage controlvm "worker1" natpf1 "tcp-port27020,tcp,,27020,,27020"
VBoxManage controlvm "worker1" natpf1 "tcp-port27021,tcp,,27021,,27021"
VBoxManage controlvm "worker1" natpf1 "tcp-port27022,tcp,,27022,,27022"


echo "########################################################################"
echo "images bauen"
echo "########################################################################"
cd ../MainServer
sbt docker:publishLocal
cd ../WorkerServer
sbt docker:publishLocal
#cd ../ActorSystem
#sbt docker:publishLocal

echo "########################################################################"
echo "Mainserver images speichern und auf worker schieben"
echo "########################################################################"
docker save -o main.tar akka-http-microservice-mainserver:1.0
docker-machine scp main.tar worker1:.
# in Manager einloggen und Images laden
docker-machine ssh worker1 docker load < main.tar
# in Manager einloggen und Images löschen
docker-machine ssh worker1 rm -rf main.tar


echo "########################################################################"
echo "Workerserver images speichern und auf worker schieben"
echo "########################################################################"
docker save -o worker.tar akka-http-microservice-workerserver:1.0
docker-machine scp worker.tar worker1:.
# in Manager einloggen Images laden
docker-machine ssh worker1 docker load<worker.tar
# in Manager einloggen und Images löschen
docker-machine ssh worker1 rm -rf worker.tar

#echo "########################################################################"
#echo "Actorsystem images speichern und auf worker schieben"
#echo "########################################################################"
#docker save -o actor.tar actorsystem:1.0
#docker-machine scp actor.tar worker:.
# in Manager einloggen Images laden
#docker-machine ssh worker docker load<actor.tar
# in Manager einloggen und Images löschen
#docker-machine ssh worker rm -rf actor.tar

docker swarm join --token $SWARMTOKEN $MANAGERIP:$MANAGERPORT
