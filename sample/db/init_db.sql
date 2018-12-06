CREATE TABLE users (
  name     VARCHAR(200) CONSTRAINT user_pk PRIMARY KEY,
  whatever int          NOT NULL
);

DO $$
BEGIN
  FOR i IN 1..100000 LOOP
    insert into users(name, whatever)
    values((SELECT MD5(random()::text) || i), i);
  END LOOP;
END;
$$ LANGUAGE plpgsql;