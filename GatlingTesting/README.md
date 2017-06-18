Gatling AntSimulation
=========================


Docker Swarm starten
---------------

Im Docker Quickstart Terminal:

```bash
$ sh createSwarmTest.sh
```

(IPs werden jetzt automatisch gesetzt)

#### Worker zum Swarm hinzufügen:

Auch im Docker Quickstart Terminal:

```bash
$ sh createWorker.sh
```

(die IP des Host-Rechners und das Swarm-Token müssen vorher angepasst werden!)


Simulation starten
-------------------

mit:

```bash
$ sbt gatling:test
```

(startet alle Tests, es gibt aber sowieso nur einen)
oder gezielt:

```bash
$ sbt gatling:testOnly ants.AntSimulation
```

(die IP des Host-Rechners muss vorher angepasst werden!)
