FROM gradle:jdk8

COPY --chown=gradle:gradle . /home/gradle/gridwars
WORKDIR /home/gradle/gridwars

VOLUME /work

CMD ["gradle", "bootRun"]
