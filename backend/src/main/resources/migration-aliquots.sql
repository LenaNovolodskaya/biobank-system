-- Миграция для перехода на структуру с аликвотами
-- Запустите вручную после обновления кода, если в samples есть данные

-- 1. Создать aliquots (Hibernate создаст при ddl-auto=update)
-- 2. Перенос: по initial_quantity создаём аликвоты с barcode-1, barcode-2, ...
--    Статусы и позиции берём из tube_status_ids и position_in_container (формат: через запятую)

DO $$
DECLARE
    r RECORD;
    i INT;
    status_ids TEXT[];
    positions TEXT[];
    sid BIGINT;
    pos TEXT;
BEGIN
    FOR r IN SELECT sample_id, barcode, initial_quantity, tube_status_ids, position_in_container, container_id 
             FROM samples 
             WHERE NOT EXISTS (SELECT 1 FROM aliquots WHERE aliquots.sample_id = samples.sample_id)
    LOOP
        status_ids := string_to_array(COALESCE(r.tube_status_ids, ''), ',');
        positions := string_to_array(COALESCE(r.position_in_container, ''), ',');
        FOR i IN 1..GREATEST(r.initial_quantity, 1) LOOP
            sid := NULLIF(TRIM(COALESCE(status_ids[i], '')), '')::BIGINT;
            pos := NULLIF(TRIM(COALESCE(positions[i], '')), '');
            INSERT INTO aliquots (barcode, sample_id, sample_status_id, container_id, position_in_container)
            VALUES (r.barcode || '-' || i, r.sample_id, sid, r.container_id, pos)
            ON CONFLICT (barcode) DO NOTHING;
        END LOOP;
    END LOOP;
END $$;
