DO
$$
BEGIN
    IF to_regclass('public.locker_reports') IS NULL THEN
        RETURN;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'locker_reports'
          AND column_name = 'locker_id'
          AND is_nullable = 'NO'
    ) THEN
        ALTER TABLE public.locker_reports
            ALTER COLUMN locker_id DROP NOT NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'locker_reports'
          AND column_name = 'is24_hours'
    ) THEN
        ALTER TABLE public.locker_reports
            ADD COLUMN is24_hours BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;
END;
$$;
