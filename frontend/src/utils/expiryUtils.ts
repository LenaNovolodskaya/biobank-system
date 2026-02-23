/**
 * Расчёт статуса срока хранения образца.
 * remainingMonths = рекомендуемый срок хранения − фактический срок хранения.
 * > 2 месяцев (включая 2 мес + дни): Годен (GREEN)
 * ≤ 2 месяцев (ровно 2 мес или меньше): Истекает (YELLOW)
 * Истёк: Просрочен (RED)
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
 */
export function getExpiryStatusCode(
  collectionDate: string | null | undefined,
  recommendedMonths: number | null | undefined
): ExpiryStatusCode {
  if (!recommendedMonths) return "GREEN";
  const startDate = getDateOnly(collectionDate);
  if (!startDate) return "GREEN";
  const expiryDate = addMonthsClamped(startDate, recommendedMonths);
  const today = getTodayDateOnly();
  if (today > expiryDate) return "RED";
  const remainingMonths =
    (expiryDate.getFullYear() - today.getFullYear()) * 12 +
    (expiryDate.getMonth() - today.getMonth());
  const adjusted =
    today.getDate() > expiryDate.getDate()
      ? remainingMonths - 1
      : remainingMonths;
  if (adjusted <= 0) return "RED"; // Срок истёк
  if (adjusted > 2) return "GREEN";
  if (adjusted < 2) return "YELLOW";
  // Ровно 2 полных месяца: если есть ещё дни до expiryDate — это "более 2 мес"
  return today.getDate() < expiryDate.getDate() ? "GREEN" : "YELLOW";
}

export function getExpiryStatusLabel(status: ExpiryStatusCode): string {
  if (status === "RED") return "Просрочен";
  if (status === "YELLOW") return "Истекает";
  return "Годен";
}
