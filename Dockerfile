FROM ubuntu:18.04 as base

ENV SBT_VERSION 1.2.8
ENV SCALA_VERSION 2.12

RUN sed -i 's/# \(.*multiverse$\)/\1/g' /etc/apt/sources.list \
  && apt-get update \
  && apt-get -y upgrade \
  && apt-get install -y \
    build-essential \
    software-properties-common \
    curl \
    default-jdk \
    ffmpeg \
    libavcodec-extra \
  && apt-get clean

RUN curl -L -o /tmp/sbt-$SBT_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb \
  && dpkg -i /tmp/sbt-$SBT_VERSION.deb \
  && rm /tmp/sbt-$SBT_VERSION.deb

ADD . /chromaprint
WORKDIR /chromaprint

RUN sbt reload

ENTRYPOINT ["/bin/bash"]

FROM base as packaged

RUN sbt "; test; package; assembly"

RUN  bash -c "source /chromaprint/target/scala-$SCALA_VERSION/classes/version.properties \
  && ln -s /chromaprint/target/scala-$SCALA_VERSION/chromaprint-assembly-\$version.jar /chromaprint-standalone.jar"

RUN echo "#!/bin/sh\n\
set -e\n\
sh -c \"java -jar /chromaprint-standalone.jar \$@\"\n\
" > /docker-entrypoint.sh \
  && chmod +x /docker-entrypoint.sh

ENTRYPOINT ["/docker-entrypoint.sh"]
