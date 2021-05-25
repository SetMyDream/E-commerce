FROM adoptopenjdk/openjdk11:alpine AS scala_app

ENV SBT_VER="1.4.7"
ENV SBT_SHA256="c2a759fe40a3c21a16b5a88d00cd66f3af6f0721e4ea61b63942dfb83a2d54fd"

ENV SCALA_VER="2.13.6"

# Install sbt
RUN set -x \
  && apk --update add --no-cache --virtual .build-deps curl \
  && SBT_URL="https://github.com/sbt/sbt/releases/download/v${SBT_VER}/sbt-${SBT_VER}.tgz" \
  && apk add shadow \
  && apk add bash \
  && apk add openssh \
  && apk add rsync \
  && apk add git \
  && curl -Ls ${SBT_URL} > /tmp/sbt.tgz \
  && sha256sum /tmp/sbt.tgz \
  && (echo "${SBT_SHA256}  /tmp/sbt.tgz" | sha256sum -c -) \
  && mkdir /opt/sbt \
  && tar -zxf /tmp/sbt.tgz -C /opt/sbt \
  && sed -i -r 's#run \"\$\@\"#unset JAVA_TOOL_OPTIONS\nrun \"\$\@\"#g' /opt/sbt/sbt/bin/sbt \
  && apk del --purge .build-deps \
  && rm -rf /tmp/sbt.tgz /var/cache/apk/*

ENV PATH="/opt/sbt/sbt/bin:$PATH" \
    JAVA_OPTS="-XX:+UseContainerSupport -Dfile.encoding=UTF-8"
# ENV SBT_OPTS="-Xmx2048M -Xss2M"

RUN mkdir /app
WORKDIR /app

# Cache plain scala binaries for version in SCALA_VER
RUN set -x \
  && echo "ThisBuild / scalaVersion := \"${SCALA_VER}\"" >> build.sbt \
  && mkdir -p project \
  && echo "sbt.version=${SBT_VER}" >> project/build.properties \
  && echo "object Test" >> Test.scala \
  && sbt compile \
  && sbt compile \
  && rm Test.scala \
  && rm -rf project \
  && rm -rf target \
  && rm build.sbt

ENTRYPOINT ["sbt"]
CMD ["run"]




FROM scala_app AS chat_service
COPY chat_service/project ./project
COPY chat_service/build.sbt .
RUN sbt update
COPY chat_service/ ./



FROM scala_app AS dispute_management_service
COPY dispute_management_service/project ./project
COPY dispute_management_service/build.sbt .
RUN sbt update
COPY dispute_management_service/ ./



FROM scala_app AS product_inventory_service
COPY product_inventory_service/project ./project
COPY product_inventory_service/build.sbt .
RUN sbt update
COPY product_inventory_service/ ./



FROM scala_app AS reporting_service
COPY reporting_service/project ./project
COPY reporting_service/build.sbt .
RUN sbt update
COPY reporting_service/ ./



FROM scala_app AS user_management_service
COPY ./user_management_service/project ./project
COPY ./user_management_service/build.sbt .
RUN sbt update
COPY ./user_management_service/ ./
