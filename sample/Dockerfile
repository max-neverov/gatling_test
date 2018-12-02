FROM openjdk:8u181-jdk-alpine

RUN	apk add --no-cache bash

COPY target/universal/gatling_test-*?.zip /tmp/gatling_test.zip

RUN mkdir -p "/opt/mn" \
    && unzip -qd "/opt/mn/" /tmp/gatling_test.zip \
    && rm -rf /tmp/gatling_test.zip

EXPOSE 8080

ENTRYPOINT ["/opt/mn/gatling_test/bin/gatling_test"]