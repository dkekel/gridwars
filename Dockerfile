FROM gradle:jdk8 as builder

COPY --chown=gradle:gradle . /home/gradle/gridwars
WORKDIR /home/gradle/gridwars
RUN ["gradle", "build"]

FROM openjdk:8-jre-slim
ARG DEPENDENCY=/home/gradle/gridwars

COPY --from=builder ${DEPENDENCY}/web/build/libs /app
COPY --from=builder ${DEPENDENCY}/core/bots/build/libs /work/bots
COPY --from=builder ${DEPENDENCY}/core/runtime/build/libs /work/runtime
COPY --from=builder ${DEPENDENCY}/core/runtime/gridwars.policy /work/runtime/gridwars.policy
COPY --from=builder ${DEPENDENCY}/core/api/build/libs /work/runtime
COPY --from=builder ${DEPENDENCY}/core/impl/build/libs /work/runtime

EXPOSE 8080 8443

ENV SPRING_PROFILES_ACTIVE prod

ENTRYPOINT ["java","-Dgridwars.directories.baseWorkDir=/work","-jar","app/gridwars-web.jar"]
