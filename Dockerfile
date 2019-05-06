FROM ubuntu:18.04

ENV SBT_VERSION 1.2.8
ENV SCALA_VERSION 2.12
ENV CHROMAPRINT_VERSION 0.1.0-SNAPSHOT

RUN \
  sed -i 's/# \(.*multiverse$\)/\1/g' /etc/apt/sources.list && \
  apt-get update && \
  apt-get -y upgrade && \
  apt-get install -y \
    build-essential \
    software-properties-common \
    curl \
    default-jdk \
    ffmpeg \
    libavcodec-extra

RUN \
  curl -L -o sbt-$SBT_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb && \
  apt-get update && \
  apt-get install -y \
    sbt

RUN mkdir /chromaprint
ADD . /chromaprint
WORKDIR /chromaprint

RUN sbt package
RUN sbt assembly

RUN echo "#!/bin/sh\n\
set -e\n\
sh -c \"java -jar /chromaprint/target/scala-$SCALA_VERSION/chromaprint-assembly-$CHROMAPRINT_VERSION.jar \$@\"\n\
" > /docker-entrypoint.sh && \
  chmod +x /docker-entrypoint.sh

ENTRYPOINT ["/docker-entrypoint.sh"]
