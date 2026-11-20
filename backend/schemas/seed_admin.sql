UPDATE users
SET role = 'ADMIN',
    status = 'ACTIVE'
WHERE LOWER(email) = LOWER('admin.demo@nn-home.test');

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM users
        WHERE LOWER(email) = LOWER('admin.demo@nn-home.test')
          AND role = 'ADMIN'
          AND status = 'ACTIVE'
    ) THEN
        RAISE EXCEPTION 'Demo admin account is not synchronized. Create the Firebase user, sign in once, then run this file again.';
    END IF;
END;
$$;
