CREATE EXTENSION IF NOT EXISTS postgis;

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

    UPDATE public.lockers
    SET location = ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)::geography
    WHERE latitude IS NOT NULL
      AND longitude IS NOT NULL
      AND (
          location IS NULL
          OR ST_X(location::geometry) IS DISTINCT FROM longitude
          OR ST_Y(location::geometry) IS DISTINCT FROM latitude
      );

    IF EXISTS (
        SELECT 1
        FROM public.lockers
        WHERE latitude IS NULL
           OR longitude IS NULL
           OR location IS NULL
    ) THEN
        RAISE EXCEPTION 'lockers latitude/longitude/location must not be null';
    END IF;

    ALTER TABLE public.lockers
        ALTER COLUMN latitude SET NOT NULL,
        ALTER COLUMN longitude SET NOT NULL,
        ALTER COLUMN location SET NOT NULL;

    ALTER TABLE public.lockers
        DROP CONSTRAINT IF EXISTS chk_lockers_latitude_range,
        DROP CONSTRAINT IF EXISTS chk_lockers_longitude_range;

    ALTER TABLE public.lockers
        ADD CONSTRAINT chk_lockers_latitude_range CHECK (latitude >= -90 AND latitude <= 90),
        ADD CONSTRAINT chk_lockers_longitude_range CHECK (longitude >= -180 AND longitude <= 180);

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

SELECT public.apply_lockers_postgis();
