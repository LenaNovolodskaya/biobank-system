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
  "reference.create": "Добавление записей в справочники",
  "reference.update": "Редактирование записей справочников",
  "reference.delete": "Удаление записей справочников",
  "reference.manage": "Полное управление справочниками",

  "role.view": "Просмотр ролей",
  "role.create": "Добавление ролей",
  "role.delete": "Удаление ролей",
  "role.permissions.manage": "Настройка разрешений ролей",
  "role.manage": "Полное управление ролями",

  "user.view": "Просмотр пользователей",
  "user.create": "Добавление пользователей",
  "user.delete": "Удаление пользователей",
  "user.permissions.manage": "Настройка разрешений пользователей",
  "user.roles.assign": "Выдача ролей пользователям",
  "user.manage": "Полное управление пользователями",
};

export function resolvePermissionLabel(code: string, label?: string | null) {
  return label || PERMISSION_LABELS[code] || code;
}
