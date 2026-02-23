export const PERMISSION_LABELS: Record<string, string> = {
  "patient.view": "Просмотр пациентов",
  "patient.create": "Создание пациентов",
  "patient.update": "Редактирование пациентов",
  "patient.delete": "Удаление пациентов",

  "visit.view": "Просмотр визитов",
  "visit.create": "Создание визитов",
  "visit.update": "Редактирование визитов",
  "visit.delete": "Удаление визитов",

  "sample.view": "Просмотр образцов",
  "sample.create": "Создание образцов",
  "sample.update": "Редактирование образцов",
  "sample.delete": "Удаление образцов",

  "research.view": "Просмотр исследований",
  "research.create": "Создание исследований",
  "research.update": "Редактирование исследований",
  "research.delete": "Удаление исследований",

  "storage.view": "Просмотр хранилища",
  "storage.create": "Создание/настройка хранилища",
  "storage.update": "Редактирование структуры хранилища",
  "storage.delete": "Удаление элементов хранилища",

  "reference.view": "Просмотр справочников",
  "reference.manage": "Редактирование справочников",

  "user.manage": "Управление пользователями",
  "role.manage": "Управление ролями и правами",
};

export function resolvePermissionLabel(code: string, label?: string | null) {
  return label || PERMISSION_LABELS[code] || code;
}
