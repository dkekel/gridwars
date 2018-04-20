#!/usr/bin/env bash
echo "Java home: $JAVA_HOME"
echo "Java bin: $(which java)"
readonly WORKDIR="$(pwd)/work"
echo "Work dir: ${WORKDIR}"
echo
echo "Starting up GridWars in the background..."

nohup java -Dspring.profiles.active=prod -Dgridwars.directories.baseWorkDir="${WORKDIR}" -jar gridwars-web.jar > "${WORKDIR}/logs/gridwars-$(date +'%d-%m-%Y-%H%M%S').log" &
