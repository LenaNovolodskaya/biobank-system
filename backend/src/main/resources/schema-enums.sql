-- Enum-типы PostgreSQL для Hibernate.
-- Выполняется до Hibernate (EnumSchemaConfig). Hibernate создаёт таблицы с ddl-auto: create/update.
DO $$
BEGIN
  CREATE TYPE gender_code_type AS ENUM ('UNKNOWN', 'MALE', 'FEMALE');
EXCEPTION WHEN duplicate_object THEN null;
END $$;
//

DO $$
BEGIN
  CREATE TYPE expiry_status_type AS ENUM ('GREEN', 'YELLOW', 'RED');
EXCEPTION WHEN duplicate_object THEN null;
END $$;
//
