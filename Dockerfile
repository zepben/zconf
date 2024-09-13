# syntax=docker/dockerfile:1

FROM docker.io/library/amazoncorretto:11-al2023 AS build

RUN yum update && yum install -y libxcrypt-compat

WORKDIR /app

COPY gradle /app/gradle
COPY gradle.properties ./gradlew settings.gradle.kts build.gradle.kts /app/

RUN ./gradlew build --no-daemon

COPY src /app/src
RUN ./gradlew linkReleaseExecutableNative

FROM scratch

COPY --from=build /app/build/bin/native/releaseExecutable/zconf.kexe /zconf

# Hack: Unfortunately Kotlin Native doesn't easily support static linking of the executable just yet. Until we do
# we need to copy any dynamic libraries. Thankfully, what we need seems to be present in our runtime images
#
# See https://dev.to/mreichelt/christmas-hacking-squeezing-kotlin-native-into-docker-6ao
COPY --from=build /lib /lib
COPY --from=build /lib64 /lib64

ENTRYPOINT [ "/zconf" ]
