/**
 * Расчёт статуса срока хранения образца.
 * remaining = рекомендованный срок − фактический срок хранения.
 * 1) Годен (GREEN): remaining > 2 месяцев
 * 2) Истекает (YELLOW): 0 < remaining ≤ 2 месяцев
 * 3) Просрочен (RED): фактический срок превышает рекомендованный (remaining ≤ 0)
 */

export type ExpiryStatusCode = "GREEN" | "YELLOW" | "RED";

function getDateOnly(value: string | null | undefined): Date | null {
  if (!value) return null;
  const normalized = value.slice(0, 10);
  const [year, month, day] = normalized.split("-").map(Number);
  if (!year || !month || !day) return null;
  return new Date(year, month - 1, day);
}

function getTodayDateOnly(): Date {
  const now = new Date();
  return new Date(now.getFullYear(), now.getMonth(), now.getDate());
}

function addMonthsClamped(date: Date, monthsToAdd: number): Date {
  const year = date.getFullYear();
  const month = date.getMonth() + monthsToAdd;
  const day = date.getDate();
  const targetYear = year + Math.floor(month / 12);
  const targetMonth = ((month % 12) + 12) % 12;
  const lastDay = new Date(targetYear, targetMonth + 1, 0).getDate();
  return new Date(targetYear, targetMonth, Math.min(day, lastDay));
}

/**
 * Вычисляет код статуса срока хранения.
 * Учитывает дни: 2 мес 4 дня = более 2 мес → Годен.
 * @param collectionDate - дата забора (ISO string)
 * @param recommendedMonths - рекомендуемый срок хранения в месяцах
 * @param todayOverride - опционально: дата "сегодня" для тестов
 */
export function getExpiryStatusCode(
  collectionDate: string | null | undefined,
  recommendedMonths: number | null | undefined,
  todayOverride?: Date
): ExpiryStatusCode {
  if (!recommendedMonths) return "GREEN";
  const startDate = getDateOnly(collectionDate);
  if (!startDate) return "GREEN";
  const expiryDate = addMonthsClamped(startDate, recommendedMonths);
  const today = todayOverride
    ? new Date(
        todayOverride.getFullYear(),
        todayOverride.getMonth(),
        todayOverride.getDate()
      )
    : getTodayDateOnly();
  if (today > expiryDate) return "RED";
  // remaining > 2 месяцев → Годен. Сравниваем expiryDate с (today + 2 мес)
  const twoMonthsFromToday = addMonthsClamped(today, 2);
  if (expiryDate > twoMonthsFromToday) return "GREEN";
  return "YELLOW";
}

export function getExpiryStatusLabel(status: ExpiryStatusCode): string {
  if (status === "RED") return "Просрочен";
  if (status === "YELLOW") return "Истекает";
  return "Годен";
}
