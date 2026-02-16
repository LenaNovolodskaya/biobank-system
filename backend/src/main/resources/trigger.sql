-- Триггер для автоматического подсчёта образцов в контейнере
-- Выполняется после data.sql (схема уже создана Hibernate при ddl-auto: create)

-- Удаляем триггер и функцию если существуют (для повторного запуска)
DROP TRIGGER IF EXISTS trg_specimens_container_count ON specimens
//
DROP FUNCTION IF EXISTS update_container_sample_count()
//
-- Функция обновления current_samples_count
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
$$ LANGUAGE plpgsql
//
-- Создаём триггер
CREATE TRIGGER trg_specimens_container_count
AFTER INSERT OR UPDATE OR DELETE ON specimens
FOR EACH ROW EXECUTE FUNCTION update_container_sample_count()
//
