-- 1) PostGIS 확장
CREATE EXTENSION IF NOT EXISTS postgis;

-- 2) lat/lng 입력 시 location(geography) 자동 동기화
CREATE OR REPLACE FUNCTION public.set_lockers_location_from_lat_lng()
RETURNS trigger
LANGUAGE plpgsql
AS
$$
BEGIN
    NEW.location := ST_SetSRID(ST_MakePoint(NEW.longitude, NEW.latitude), 4326)::geography;
    RETURN NEW;
END;
$$;

-- 3) lockers 테이블에 필요한 최소 구성(location, trigger, index) 적용 함수
CREATE OR REPLACE FUNCTION public.apply_lockers_postgis()
RETURNS void
LANGUAGE plpgsql
AS
$$
BEGIN
    IF to_regclass('public.lockers') IS NULL THEN
        RETURN;
    END IF;

    ALTER TABLE public.lockers
        ADD COLUMN IF NOT EXISTS location geography(Point, 4326);

    DROP TRIGGER IF EXISTS trg_lockers_set_location ON public.lockers;
    CREATE TRIGGER trg_lockers_set_location
        BEFORE INSERT OR UPDATE OF latitude, longitude ON public.lockers
        FOR EACH ROW
    EXECUTE FUNCTION public.set_lockers_location_from_lat_lng();

    CREATE INDEX IF NOT EXISTS idx_lockers_location_geog
        ON public.lockers
        USING GIST (location);
END;
$$;

-- 4) 현재 lockers가 있으면 즉시 적용
SELECT public.apply_lockers_postgis();

-- 5) lockers가 나중에 생성돼도 자동 적용
CREATE OR REPLACE FUNCTION public.on_create_table_apply_lockers_postgis()
RETURNS event_trigger
LANGUAGE plpgsql
AS
$$
BEGIN
    PERFORM public.apply_lockers_postgis();
END;
$$;

DROP EVENT TRIGGER IF EXISTS evt_apply_lockers_postgis_on_create;
CREATE EVENT TRIGGER evt_apply_lockers_postgis_on_create
    ON ddl_command_end
    WHEN TAG IN ('CREATE TABLE')
EXECUTE FUNCTION public.on_create_table_apply_lockers_postgis();
