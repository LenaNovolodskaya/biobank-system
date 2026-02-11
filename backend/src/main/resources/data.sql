-- =============================================
-- Значения по умолчанию для справочников.
-- Выполняется после создания схемы Hibernate (create/create-drop).
-- ON CONFLICT DO NOTHING — при повторном запуске (ddl-auto=update) дубликаты не вызывают ошибку.
-- =============================================

-- 1) Гендер пациента (тип gender_code_type) — задаётся в схеме при создании ENUM:
--    'UNKNOWN', 'MALE', 'FEMALE'. DEFAULT для patients.gender: 'UNKNOWN'.

-- 2) Статус срока годности (тип expiry_status_type) — задаётся в схеме при создании ENUM:
--    'GREEN', 'YELLOW', 'RED'. DEFAULT для samples.expiry_status: 'GREEN'.

-- 3) Национальности
INSERT INTO nationalities (nationality_name) VALUES
    ('Армянин'), ('Азербайджанец'), ('Белорус'), ('Бурят'), ('Немец'),
    ('Русский'), ('Украинец'), ('Не указано')
ON CONFLICT (nationality_name) DO NOTHING;

-- 4) Статусы образцов
INSERT INTO sample_statuses (sample_status_name) VALUES
    ('В хранилище'), ('Изъят'), ('Утилизирован'), ('Исчерпан')
ON CONFLICT (sample_status_name) DO NOTHING;

-- 5) Типы образцов (с путями к иконкам)
INSERT INTO sample_types (sample_type_name, icon_path) VALUES
    ('Кровь', 'blood.svg'),
    ('Плазма', 'plasma.svg'),
    ('Сыворотка', 'serum.svg'),
    ('Моча', 'urine.svg'),
    ('Кал', 'stool.svg'),
    ('Биопсия', 'biopsy.svg'),
    ('Лейкоциты', 'leukocytes.svg'),
    ('гемолизаты', 'hemolysates.svg'),
    ('ДНК', 'dna.svg'),
    ('РНК', 'rna.svg'),
    ('Десневая жидкость', 'gingival-fluid.svg'),
    ('Слюна', 'saliva.svg'),
    ('Парафиновые блоки', 'paraffin-blocks.svg'),
    ('Гистологические стёкла', 'histology-slides.svg'),
    ('Волосы', 'hair.svg')
ON CONFLICT (sample_type_name) DO NOTHING;

-- 6) Шаблоны контейнеров (типы нумерации: LETTER_DIGIT, DIGIT_LETTER, DIGIT_DIGIT, SEQUENTIAL)
INSERT INTO container_type_templates (template_name, rows_count, columns_count, numbering_type, display_order) VALUES
    ('Криобокс для низких температур (10х10)', 10, 10, 'LETTER_DIGIT', 1),
    ('Криобокс для низких температур (9х9)', 9, 9, 'LETTER_DIGIT', 2),
    ('Штатив для микропробирок 1.5-2 мл (10х5)', 10, 5, 'SEQUENTIAL', 3),
    ('Штатив для микропробирок 1.5-2 мл (8х12)', 8, 12, 'SEQUENTIAL', 4),
    ('Штатив/кассета для пробирок 5-15 мл (5х5)', 5, 5, 'SEQUENTIAL', 5),
    ('Транспортная стойка для анализаторов (8х5)', 8, 5, 'SEQUENTIAL', 6)
ON CONFLICT (template_name) DO NOTHING;
