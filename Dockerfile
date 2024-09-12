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
COPY --from=build /lib /lib
COPY --from=build /lib64 /lib64

ENTRYPOINT [ "/zconf" ]
