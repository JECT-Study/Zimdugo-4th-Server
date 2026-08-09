DO
$$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'users'
          AND column_name = 'nickname'
    ) THEN
        ALTER TABLE public.users
            DROP COLUMN nickname;
    END IF;
END;
$$;;
