#!/bin/bash

set -e

export PGUSER="$POSTGRES_USER"

"${psql[@]}" <<- 'EOSQL'
CREATE DATABASE template_postgis IS_TEMPLATE true;
EOSQL

for DB in template_postgis "$POSTGRES_DB"; do
    echo "Loading PostGIS extensions into $DB"
    "${psql[@]}" --dbname="$DB" <<-'EOSQL'
        CREATE EXTENSION IF NOT EXISTS postgis;
        CREATE EXTENSION IF NOT EXISTS postgis_topology;
        \c
        DO $$
        DECLARE
            postgis_major integer;
            postgis_minor integer;
        BEGIN
            SELECT substring(postgis_lib_version() from '^([0-9]+)')::integer,
                substring(postgis_lib_version() from '^[0-9]+\.([0-9]+)')::integer
            INTO postgis_major, postgis_minor;

            IF postgis_major < 3 OR (postgis_major = 3 AND postgis_minor < 7) THEN
                CREATE EXTENSION IF NOT EXISTS fuzzystrmatch;
                CREATE EXTENSION IF NOT EXISTS postgis_tiger_geocoder;
            END IF;
        END
        $$;
EOSQL
done
