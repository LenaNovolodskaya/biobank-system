-- =============================================
-- === Создание ENUM типов (идемпотентно) ===
-- =============================================
DO $$ BEGIN
  CREATE TYPE gender_code_type AS ENUM ('UNKNOWN', 'MALE', 'FEMALE');
EXCEPTION WHEN duplicate_object THEN null; END $$;
COMMENT ON TYPE gender_code_type IS '0=Неизвестно, 1=Мужской, 2=Женский';
//

DO $$ BEGIN
  CREATE TYPE expiry_status_type AS ENUM ('GREEN', 'YELLOW', 'RED');
EXCEPTION WHEN duplicate_object THEN null; END $$;
COMMENT ON TYPE expiry_status_type IS 'green=годен, yellow=истекает, red=просрочен';
//

DO $$ BEGIN
  CREATE TYPE numbering_type_enum AS ENUM ('LETTER_DIGIT', 'DIGIT_LETTER', 'DIGIT_DIGIT', 'SEQUENTIAL');
EXCEPTION WHEN duplicate_object THEN null; END $$;
COMMENT ON TYPE numbering_type_enum IS 'Тип нумерации позиций в контейнере';
//

-- =============================================
-- === Основные сущности (пациенты, исследования, визиты) ===
-- =============================================

-- Таблица национальностей (справочник)
CREATE TABLE IF NOT EXISTS nationalities (
    nationality_id SERIAL PRIMARY KEY,
    nationality_name VARCHAR(100) UNIQUE NOT NULL
);
COMMENT ON TABLE nationalities IS 'Справочник национальностей';
//

-- Диагнозы по МКБ-10 (справочник)
CREATE TABLE IF NOT EXISTS diagnoses (
    diagnosis_id SERIAL PRIMARY KEY,
    icd10_code VARCHAR(10) UNIQUE,
    diagnosis_name TEXT NOT NULL
);
//

-- Таблица пациентов
CREATE TABLE IF NOT EXISTS patients (
    patient_id SERIAL PRIMARY KEY,
    patient_barcode VARCHAR(100) UNIQUE NOT NULL,
    gender gender_code_type DEFAULT 'UNKNOWN' NOT NULL,
    birth_date DATE NOT NULL,
    nationality_id INT,
    main_diagnosis_id INT,
    created_at_patient TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    informed_consent BOOLEAN,
    
    CONSTRAINT fk_patients_nationality 
        FOREIGN KEY (nationality_id) 
        REFERENCES nationalities(nationality_id)
        ON DELETE SET NULL,

    CONSTRAINT fk_patients_main_diagnosis
        FOREIGN KEY (main_diagnosis_id)
        REFERENCES diagnoses(diagnosis_id)
        ON DELETE SET NULL
);
COMMENT ON TABLE patients IS 'Пациенты (доноры биообразцов)';
//

-- Сопутствующие диагнозы для пациентов (многие-ко-многим)
CREATE TABLE IF NOT EXISTS patient_comorbid_diagnoses (
    patient_id INT NOT NULL,
    diagnosis_id INT NOT NULL,
    PRIMARY KEY (patient_id, diagnosis_id),

    CONSTRAINT fk_patient_comorbid_patient
        FOREIGN KEY (patient_id)
        REFERENCES patients(patient_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_patient_comorbid_diagnosis
        FOREIGN KEY (diagnosis_id)
        REFERENCES diagnoses(diagnosis_id)
);
//

-- Группы исследований (справочник)
CREATE TABLE IF NOT EXISTS research_groups (
    research_group_id SERIAL PRIMARY KEY,
    research_group_name VARCHAR(100) UNIQUE NOT NULL
);
//

-- Источники финансирования (справочник)
CREATE TABLE IF NOT EXISTS financing_sources (
    financing_source_id SERIAL PRIMARY KEY,
    financing_source_name VARCHAR(100) UNIQUE NOT NULL
);
//

-- Подразделения (справочник)
CREATE TABLE IF NOT EXISTS departments (
    department_id SERIAL PRIMARY KEY,
    department_name VARCHAR(255) UNIQUE NOT NULL
);
//

-- Исследования
CREATE TABLE IF NOT EXISTS researches (
    research_id SERIAL PRIMARY KEY,
    research_number VARCHAR(100) UNIQUE NOT NULL,
    research_name TEXT NOT NULL,
    research_group_id INT,
    financing_source_id INT,
    department_id INT,
    is_active BOOLEAN DEFAULT TRUE,
    
    CONSTRAINT fk_researches_group 
        FOREIGN KEY (research_group_id) 
        REFERENCES research_groups(research_group_id),
    
    CONSTRAINT fk_researches_financing 
        FOREIGN KEY (financing_source_id) 
        REFERENCES financing_sources(financing_source_id),
    
    CONSTRAINT fk_researches_department 
        FOREIGN KEY (department_id) 
        REFERENCES departments(department_id)
);
//

-- Визиты (забор биоматериала) — диагноз берётся из пациента
CREATE TABLE IF NOT EXISTS visits (
    visit_id SERIAL PRIMARY KEY,
    patient_id INT NOT NULL,
    research_id INT NOT NULL,
    visit_number INT NOT NULL,
    collection_date TIMESTAMP NOT NULL,
    age_at_collection INT NOT NULL,
    
    CONSTRAINT fk_visits_patient 
        FOREIGN KEY (patient_id) 
        REFERENCES patients(patient_id)
        ON DELETE CASCADE,
    
    CONSTRAINT fk_visits_research 
        FOREIGN KEY (research_id) 
        REFERENCES researches(research_id),
    
    CONSTRAINT uq_visit_patient_number 
        UNIQUE (patient_id, visit_number)
);
COMMENT ON TABLE visits IS 'Визиты пациентов для забора биоматериала';
//

-- =============================================
-- === Биообразцы и их жизненный цикл ===
-- =============================================

-- Типы образцов (справочник)
CREATE TABLE IF NOT EXISTS sample_types (
    sample_type_id SERIAL PRIMARY KEY,
    sample_type_name VARCHAR(100) UNIQUE NOT NULL,
    icon_path VARCHAR(255)
);
//

-- Статусы образцов (справочник)
CREATE TABLE IF NOT EXISTS sample_statuses (
    sample_status_id SERIAL PRIMARY KEY,
    sample_status_name VARCHAR(50) UNIQUE NOT NULL
);
//

-- Таблица биообразцов (общие поля образца)
CREATE TABLE IF NOT EXISTS samples (
    sample_id SERIAL PRIMARY KEY,
    visit_id INT NOT NULL,
    barcode VARCHAR(100) UNIQUE NOT NULL,
    sample_type_id INT,
    initial_quantity INT NOT NULL CHECK (initial_quantity > 0),
    current_quantity INT NOT NULL CHECK (current_quantity >= 0),
    recommended_storage_months INT,
    actual_storage_months INT,
    expiry_status expiry_status_type DEFAULT 'GREEN',
    container_id INT,
    created_at_sample TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_samples_visit 
        FOREIGN KEY (visit_id) 
        REFERENCES visits(visit_id)
        ON DELETE CASCADE,
    
    CONSTRAINT fk_samples_type 
        FOREIGN KEY (sample_type_id) 
        REFERENCES sample_types(sample_type_id),
    
    CONSTRAINT chk_quantity CHECK (current_quantity <= initial_quantity)
);
//

-- Таблица образцов (пробирки с уникальным штрихкодом, статусом и позицией)
CREATE TABLE IF NOT EXISTS specimens (
    specimen_id SERIAL PRIMARY KEY,
    barcode VARCHAR(100) UNIQUE NOT NULL,
    sample_id INT NOT NULL,
    sample_status_id INT,
    container_id INT,
    position_in_container VARCHAR(50),
    
    CONSTRAINT fk_specimens_sample 
        FOREIGN KEY (sample_id) 
        REFERENCES samples(sample_id)
        ON DELETE CASCADE,
    
    CONSTRAINT fk_specimens_status 
        FOREIGN KEY (sample_status_id) 
        REFERENCES sample_statuses(sample_status_id)
);
//

-- Типы транзакций (справочник)
CREATE TABLE IF NOT EXISTS transaction_types (
    transaction_type_id SERIAL PRIMARY KEY,
    transaction_type_name VARCHAR(50) UNIQUE NOT NULL
);
//

-- Движение образцов (аудит)
CREATE TABLE IF NOT EXISTS sample_transactions (
    transaction_id SERIAL PRIMARY KEY,
    sample_id INT NOT NULL,
    user_id INT NOT NULL,
    transaction_type_id INT,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    quantity_change INT NOT NULL,
    department_id INT,
    purpose TEXT,
    notes TEXT,
    
    CONSTRAINT fk_transactions_sample 
        FOREIGN KEY (sample_id) 
        REFERENCES samples(sample_id)
        ON DELETE CASCADE,
    
    CONSTRAINT fk_transactions_type 
        FOREIGN KEY (transaction_type_id) 
        REFERENCES transaction_types(transaction_type_id),
    
    CONSTRAINT fk_transactions_department 
        FOREIGN KEY (department_id) 
        REFERENCES departments(department_id)
);
//

-- =============================================
-- === Пользователи и права доступа ===
-- =============================================

-- Пользователи системы
CREATE TABLE IF NOT EXISTS users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(150),
    is_active BOOLEAN DEFAULT TRUE
);
//
COMMENT ON COLUMN users.password IS 'Хэш пароля (BCrypt)';
//

-- Роли пользователей
CREATE TABLE IF NOT EXISTS roles (
    role_id SERIAL PRIMARY KEY,
    role_name VARCHAR(50) UNIQUE NOT NULL
);
//

-- Разрешения (действия)
CREATE TABLE IF NOT EXISTS permissions (
    permission_id SERIAL PRIMARY KEY,
    permission_name VARCHAR(100) UNIQUE NOT NULL
);
//

-- Связь пользователь-роль (многие-ко-многим)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    
    PRIMARY KEY (user_id, role_id),
    
    CONSTRAINT fk_user_roles_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(user_id)
        ON DELETE CASCADE,
    
    CONSTRAINT fk_user_roles_role 
        FOREIGN KEY (role_id) 
        REFERENCES roles(role_id)
        ON DELETE CASCADE
);
//

-- Связь роль-разрешение (многие-ко-многим)
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id INT NOT NULL,
    permission_id INT NOT NULL,
    
    PRIMARY KEY (role_id, permission_id),
    
    CONSTRAINT fk_role_permissions_role 
        FOREIGN KEY (role_id) 
        REFERENCES roles(role_id)
        ON DELETE CASCADE,
    
    CONSTRAINT fk_role_permissions_permission 
        FOREIGN KEY (permission_id) 
        REFERENCES permissions(permission_id)
        ON DELETE CASCADE
);
//

-- =============================================
-- === Хранилище ===
-- =============================================

-- Типы хранилища (справочник)
CREATE TABLE IF NOT EXISTS unit_types (
    unit_type_id SERIAL PRIMARY KEY,
    unit_type_name VARCHAR(100) UNIQUE NOT NULL
);
//

-- Физические локации
CREATE TABLE IF NOT EXISTS storage_locations (
    location_id SERIAL PRIMARY KEY,
    location_name VARCHAR(150) NOT NULL,
    address VARCHAR(255) NOT NULL,
    room_number VARCHAR(50) NOT NULL,
    description TEXT
);
//

-- Единицы хранения (холодильники, шкафы)
CREATE TABLE IF NOT EXISTS storage_units (
    unit_id SERIAL PRIMARY KEY,
    location_id INT NOT NULL,
    unit_type_id INT NOT NULL,
    unit_name VARCHAR(100) NOT NULL,
    shelves_count INT,
    
    CONSTRAINT fk_storage_units_location 
        FOREIGN KEY (location_id) 
        REFERENCES storage_locations(location_id)
        ON DELETE CASCADE,
    
    CONSTRAINT fk_storage_units_unit_type 
        FOREIGN KEY (unit_type_id) 
        REFERENCES unit_types(unit_type_id)
);
//

-- Шаблоны типов контейнеров
CREATE TABLE IF NOT EXISTS container_type_templates (
    template_id SERIAL PRIMARY KEY,
    template_name VARCHAR(255) UNIQUE NOT NULL,
    rows_count INT NOT NULL CHECK (rows_count > 0),
    columns_count INT NOT NULL CHECK (columns_count > 0),
    max_samples_count INT NOT NULL CHECK (max_samples_count > 0),
    numbering_type numbering_type_enum NOT NULL
);
//

-- Контейнеры (коробки, штативы)
CREATE TABLE IF NOT EXISTS storage_containers (
    container_id SERIAL PRIMARY KEY,
    container_number VARCHAR(255),
    current_samples_count INT NOT NULL DEFAULT 0 CHECK (current_samples_count >= 0),
    unit_id INT NOT NULL,
    shelf_number INT,
    template_id INT NOT NULL,
    
    CONSTRAINT fk_storage_containers_unit 
        FOREIGN KEY (unit_id) 
        REFERENCES storage_units(unit_id)
        ON DELETE CASCADE,
    
    CONSTRAINT fk_storage_containers_template 
        FOREIGN KEY (template_id) 
        REFERENCES container_type_templates(template_id)
);
//

-- Функция обновления current_samples_count для контейнера
CREATE OR REPLACE FUNCTION update_container_sample_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' AND NEW.container_id IS NOT NULL THEN
        UPDATE storage_containers 
        SET current_samples_count = current_samples_count + 1 
        WHERE container_id = NEW.container_id;
    ELSIF TG_OP = 'DELETE' AND OLD.container_id IS NOT NULL THEN
        UPDATE storage_containers 
        SET current_samples_count = GREATEST(0, current_samples_count - 1) 
        WHERE container_id = OLD.container_id;
    ELSIF TG_OP = 'UPDATE' THEN
        IF OLD.container_id IS NOT NULL AND (NEW.container_id IS NULL OR NEW.container_id != OLD.container_id) THEN
            UPDATE storage_containers 
            SET current_samples_count = GREATEST(0, current_samples_count - 1) 
            WHERE container_id = OLD.container_id;
        END IF;
        IF NEW.container_id IS NOT NULL AND (OLD.container_id IS NULL OR NEW.container_id != OLD.container_id) THEN
            UPDATE storage_containers 
            SET current_samples_count = current_samples_count + 1 
            WHERE container_id = NEW.container_id;
        END IF;
    END IF;
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;
//

-- Триггер для автоматического подсчёта образцов в контейнере (идемпотентно)
DROP TRIGGER IF EXISTS trg_specimens_container_count ON specimens;
CREATE TRIGGER trg_specimens_container_count
AFTER INSERT OR UPDATE OR DELETE ON specimens
FOR EACH ROW EXECUTE FUNCTION update_container_sample_count();
//

-- Добавляем внешние ключи на контейнеры (идемпотентно)
DO $$ BEGIN
  ALTER TABLE samples ADD CONSTRAINT fk_samples_container 
    FOREIGN KEY (container_id) REFERENCES storage_containers(container_id) ON DELETE SET NULL;
EXCEPTION WHEN duplicate_object THEN null; END $$;
//

DO $$ BEGIN
  ALTER TABLE specimens ADD CONSTRAINT fk_specimens_container 
    FOREIGN KEY (container_id) REFERENCES storage_containers(container_id) ON DELETE SET NULL;
EXCEPTION WHEN duplicate_object THEN null; END $$;
//

-- Справочные данные по умолчанию загружаются из data.sql после создания схемы
