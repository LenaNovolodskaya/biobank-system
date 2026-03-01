/**
 * Проверка логики expiryUtils.
 * Запуск: cd frontend && node --experimental-vm-modules node_modules/vite/bin/vite.js или
 *        npx tsx src/utils/expiryUtils.verification.mjs
 * Проще: node src/utils/expiryUtils.verification.mjs (если переименовать в .cjs и использовать require)
 *
 * Или через eval с импортом скомпилированного модуля.
 * Здесь — ручная проверка той же логики.
 */

// Копия логики для проверки (без зависимости от Date)
function addMonthsClamped(year, month, day, monthsToAdd) {
  const targetMonth = month + monthsToAdd;
  const targetYear = year + Math.floor(targetMonth / 12);
  const m = ((targetMonth % 12) + 12) % 12;
  const lastDay = new Date(targetYear, m + 1, 0).getDate();
  return { year: targetYear, month: m, day: Math.min(day, lastDay) };
}

function addMonthsToDate(year, month, day, monthsToAdd) {
  const targetMonth = month + monthsToAdd;
  const targetYear = year + Math.floor(targetMonth / 12);
  const m = ((targetMonth % 12) + 12) % 12;
  const lastDay = new Date(targetYear, m + 1, 0).getDate();
  return new Date(targetYear, m, Math.min(day, lastDay));
}

function getStatusCode(collectionDate, recommendedMonths, today) {
  const [cy, cm, cd] = collectionDate.split("-").map(Number);
  const expiry = addMonthsClamped(cy, cm - 1, cd, recommendedMonths);
  const expiryDate = new Date(expiry.year, expiry.month, expiry.day);
  const todayDate = new Date(today.year, today.month, today.day);

  if (todayDate > expiryDate) return "RED"; // Просрочен

  const twoMonthsFromToday = addMonthsToDate(
    today.year,
    today.month,
    today.day,
    2
  );
  if (expiryDate > twoMonthsFromToday) return "GREEN";
  return "YELLOW";
}

const tests = [
  // [collectionDate, recommendedMonths, today, expected]
  // Фактический 1 мес 12 дней, рекомендовано 2 мес → осталось ~18 дней → Истекает
  { collection: "2024-12-01", recommended: 2, today: "2025-01-13", expected: "YELLOW" },
  // Фактический 0, рекомендовано 6 мес → осталось 6 мес → Годен
  { collection: "2024-07-15", recommended: 6, today: "2024-07-15", expected: "GREEN" },
  // Фактический 3 мес, рекомендовано 6 мес → осталось 3 мес → Годен
  { collection: "2024-04-15", recommended: 6, today: "2024-07-15", expected: "GREEN" },
  // Фактический 4 мес, рекомендовано 6 мес → осталось 2 мес → Истекает
  { collection: "2024-03-15", recommended: 6, today: "2024-07-15", expected: "YELLOW" },
  // Фактический 6 мес, рекомендовано 6 мес → последний день → Истекает
  { collection: "2024-01-15", recommended: 6, today: "2024-07-15", expected: "YELLOW" },
  // Фактический 6 мес 1 день, рекомендовано 6 мес → Просрочен
  { collection: "2024-01-15", recommended: 6, today: "2024-07-16", expected: "RED" },
  // Ровно 2 мес осталось → Истекает
  { collection: "2024-03-15", recommended: 4, today: "2024-05-15", expected: "YELLOW" },
  // 2 мес 20 дней осталось (июнь 5 — авг 25) → Годен
  { collection: "2024-04-05", recommended: 5, today: "2024-06-10", expected: "GREEN" },
];

console.log("Проверка логики статуса срока хранения:\n");

let passed = 0;
for (const t of tests) {
  const [ty, tm, td] = t.today.split("-").map(Number);
  const result = getStatusCode(t.collection, t.recommended, {
    year: ty,
    month: tm - 1,
    day: td,
  });
  const ok = result === t.expected;
  if (ok) passed++;
  console.log(
    ok ? "✓" : "✗",
    `Забор ${t.collection}, рекомендовано ${t.recommended} мес, сегодня ${t.today} → ${result} (ожидалось ${t.expected})`
  );
}

console.log(`\n${passed}/${tests.length} тестов пройдено`);
process.exit(passed === tests.length ? 0 : 1);
