#!/usr/bin/env bash
echo "Java home: $JAVA_HOME"
echo "Java bin: $(which java)"
readonly WORKDIR="$(pwd)/work"
echo "Work dir: ${WORKDIR}"
echo
echo "Starting up GridWars in the background..."

readonly JVM_ARGS="-Xms2048m -Xmx2048m"
readonly SYS_PROP_ARGS="-Dspring.profiles.active=prod -Dgridwars.directories.baseWorkDir=${WORKDIR}"

nohup java ${JVM_ARGS} ${SYS_PROP_ARGS} -jar gridwars-web.jar > "${WORKDIR}/logs/gridwars-$(date +'%d-%m-%Y-%H%M%S').log" &
