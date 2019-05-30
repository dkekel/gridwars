FROM gradle:jdk11

COPY --chown=gradle:gradle . /home/gradle/gridwars
WORKDIR /home/gradle/gridwars

CMD ["gradle", "bootRun"]
