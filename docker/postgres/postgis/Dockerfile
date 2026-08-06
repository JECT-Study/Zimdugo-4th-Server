# Derived from the PostGIS project's docker-postgis 16-3.5 image.
# Source: https://github.com/postgis/docker-postgis/tree/master/16-3.5
FROM docker.io/postgres:16-bullseye@sha256:32dc7997967900b97fe974553384423d5e01fe6227947ff05630831b3f2e36c8

LABEL maintainer="Zimdugo" \
      org.opencontainers.image.description="ARM64-compatible PostgreSQL 16 with PostGIS 3.5" \
      org.opencontainers.image.source="https://github.com/JECT-Study/Zimdugo-4th-Server"

ENV POSTGIS_MAJOR=3
ENV POSTGIS_VERSION=3.5.2+dfsg-1.pgdg110+1

RUN apt-get update \
      && apt-cache showpkg postgresql-$PG_MAJOR-postgis-$POSTGIS_MAJOR \
      && apt-get install -y --no-install-recommends \
           ca-certificates \
           postgresql-$PG_MAJOR-postgis-$POSTGIS_MAJOR=$POSTGIS_VERSION \
           postgresql-$PG_MAJOR-postgis-$POSTGIS_MAJOR-scripts \
      && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /docker-entrypoint-initdb.d
COPY ./initdb-postgis.sh /docker-entrypoint-initdb.d/10_postgis.sh
COPY ./update-postgis.sh /usr/local/bin
