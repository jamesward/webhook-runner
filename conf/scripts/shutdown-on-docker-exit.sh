#!/bin/bash

while true; do
  uptime=$(cat /proc/uptime | awk '{printf "%0.f", $1}')

  num_docker_ps=$(docker ps | grep -v stackdriver-logging-agent | tail -n +2 | wc -l)

  if ((num_docker_ps == "0")) && ((uptime > 30)); then
    echo "no docker processes running, so shutting down"
    poweroff
  fi

  sleep 1

done
