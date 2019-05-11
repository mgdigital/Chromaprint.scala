FROM ubuntu:18.04 as base

ENV SBT_VERSION 1.2.8
ENV SCALA_VERSION 2.12

ADD ./scripts/install_deps /tmp/install_deps

RUN /tmp/install_deps \
  && rm /tmp/install_deps

ENTRYPOINT ["/bin/bash"]

FROM base as sources

ADD . /chromaprint
WORKDIR /chromaprint

RUN sbt reload

FROM sources as packaged

RUN sbt "; test; package; assembly"

ENTRYPOINT ["./scripts/chromaprint"]
