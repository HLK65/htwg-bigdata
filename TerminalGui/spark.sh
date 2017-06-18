#!/bin/bash
curl -H "Content-Type: application/json" -X POST -d '{"collection":"demo2","x":"250","y":"250","timestep":"5000"}' http://localhost:9500/grid -o ~/desktop/simulation.json

