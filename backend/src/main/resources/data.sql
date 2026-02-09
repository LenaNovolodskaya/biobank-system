-- =============================================
-- Значения по умолчанию для справочников.
-- Выполняется после создания схемы Hibernate (create/create-drop).
-- ON CONFLICT DO NOTHING — при повторном запуске (ddl-auto=update) дубликаты не вызывают ошибку.
-- =============================================

INSERT INTO nationalities (nationality_name) VALUES
    ('Армянин'), ('Азербайджанец'), ('Белорус'), ('Бурят'), ('Еврей'),
    ('Калмык'), ('Немец'), ('Русский'), ('Татарин'), ('Украинец'),
    ('Не указано')
ON CONFLICT (nationality_name) DO NOTHING;

INSERT INTO sample_statuses (sample_status_name)
SELECT 'В хранилище'
WHERE NOT EXISTS (
    SELECT 1 FROM sample_statuses WHERE sample_status_name = 'В хранилище'
);
INSERT INTO sample_statuses (sample_status_name)
SELECT 'Изъят'
WHERE NOT EXISTS (
    SELECT 1 FROM sample_statuses WHERE sample_status_name = 'Изъят'
);
INSERT INTO sample_statuses (sample_status_name)
SELECT 'Утилизирован'
WHERE NOT EXISTS (
    SELECT 1 FROM sample_statuses WHERE sample_status_name = 'Утилизирован'
);
INSERT INTO sample_statuses (sample_status_name)
SELECT 'Исчерпан'
WHERE NOT EXISTS (
    SELECT 1 FROM sample_statuses WHERE sample_status_name = 'Исчерпан'
);

INSERT INTO sample_types (sample_type_name)
SELECT 'Кровь'
WHERE NOT EXISTS (
    SELECT 1 FROM sample_types WHERE sample_type_name = 'Кровь'
);
INSERT INTO sample_types (sample_type_name)
SELECT 'Плазма'
WHERE NOT EXISTS (
    SELECT 1 FROM sample_types WHERE sample_type_name = 'Плазма'
);
INSERT INTO sample_types (sample_type_name)
SELECT 'Сыворотка'
WHERE NOT EXISTS (
    SELECT 1 FROM sample_types WHERE sample_type_name = 'Сыворотка'
);
INSERT INTO sample_types (sample_type_name)
SELECT 'Моча'
WHERE NOT EXISTS (
    SELECT 1 FROM sample_types WHERE sample_type_name = 'Моча'
);
INSERT INTO sample_types (sample_type_name)
SELECT 'Слюна'
WHERE NOT EXISTS (
    SELECT 1 FROM sample_types WHERE sample_type_name = 'Слюна'
);
INSERT INTO sample_types (sample_type_name)
SELECT 'Ткань'
WHERE NOT EXISTS (
    SELECT 1 FROM sample_types WHERE sample_type_name = 'Ткань'
);
INSERT INTO sample_types (sample_type_name)
SELECT 'ДНК'
WHERE NOT EXISTS (
    SELECT 1 FROM sample_types WHERE sample_type_name = 'ДНК'
);
INSERT INTO sample_types (sample_type_name)
SELECT 'РНК'
WHERE NOT EXISTS (
    SELECT 1 FROM sample_types WHERE sample_type_name = 'РНК'
);
