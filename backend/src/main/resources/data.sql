-- =============================================
-- Значения по умолчанию для справочников.
-- Выполняется после создания схемы.
-- ON CONFLICT DO NOTHING — при повторном запуске дубликаты не вызывают ошибку.
-- =============================================

-- 1) Гендер пациента (тип gender_code_type): 'UNKNOWN', 'MALE', 'FEMALE'
-- 2) Статус срока годности (тип expiry_status_type): 'GREEN', 'YELLOW', 'RED'
-- 3) Тип нумерации (тип numbering_type_enum): 'LETTER_DIGIT', 'DIGIT_LETTER', 'DIGIT_DIGIT', 'SEQUENTIAL'

-- Национальности
INSERT INTO nationalities (nationality_name) VALUES
    ('Армянин'), ('Азербайджанец'), ('Белорус'), ('Бурят'), ('Немец'),
    ('Русский'), ('Украинец'), ('Не указано')
ON CONFLICT (nationality_name) DO NOTHING
//
-- Статусы образцов (только 3 типа)
INSERT INTO sample_statuses (sample_status_name) VALUES
    ('В хранилище'), ('Изъят'), ('Исчерпан')
ON CONFLICT (sample_status_name) DO NOTHING
//
-- Типы образцов (с путями к иконкам)
INSERT INTO sample_types (sample_type_name, icon_path) VALUES
    ('Кровь', 'blood.svg'),
    ('Плазма', 'plasma.svg'),
    ('Сыворотка', 'serum.svg'),
    ('Моча', 'urine.svg'),
    ('Кал', 'stool.svg'),
    ('Биопсия', 'biopsy.svg'),
    ('Лейкоциты', 'leukocytes.svg'),
    ('Гемолизаты', 'hemolysates.svg'),
    ('ДНК', 'dna.svg'),
    ('РНК', 'rna.svg'),
    ('Десневая жидкость', 'gingival-fluid.svg'),
    ('Слюна', 'saliva.svg'),
    ('Парафиновые блоки', 'paraffin-blocks.svg'),
    ('Гистологические стёкла', 'histology-slides.svg'),
    ('Волосы', 'hair.svg')
ON CONFLICT (sample_type_name) DO NOTHING
//
-- Шаблоны контейнеров (без display_order, по id)
INSERT INTO container_type_templates (template_name, rows_count, columns_count, max_samples_count, numbering_type)
SELECT v.* FROM (VALUES
    ('Криобокс для низких температур', 10, 10, 100, 'LETTER_DIGIT'),
    ('Криобокс для низких температур', 9, 9, 81, 'LETTER_DIGIT'),
    ('Штатив для микропробирок 1.5-2 мл', 10, 5, 50, 'SEQUENTIAL'),
    ('Штатив для микропробирок 1.5-2 мл', 8, 12, 96, 'SEQUENTIAL'),
    ('Штатив/кассета для пробирок 5-15 мл', 5, 5, 25, 'SEQUENTIAL'),
    ('Транспортная стойка для анализаторов', 8, 5, 40, 'SEQUENTIAL')
) AS v(template_name, rows_count, columns_count, max_samples_count, numbering_type)
WHERE (SELECT COUNT(*) FROM container_type_templates) = 0
//
-- Типы хранилища
INSERT INTO unit_types (unit_type_name) VALUES
    ('Холодильник'), ('Шкаф')
ON CONFLICT (unit_type_name) DO NOTHING
//
