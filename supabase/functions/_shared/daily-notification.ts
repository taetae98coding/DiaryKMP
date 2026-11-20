import type { FcmMessage, FcmTarget } from "./fcm.ts";

export const DAILY_NOTIFICATION_COLLAPSE_ID = "daily-notification";
// 앱의 core:fcm:impl DailyNotificationChannel의 채널 ID와 같아야 그 채널로 표시된다.
export const DAILY_NOTIFICATION_ANDROID_CHANNEL_ID = "dailyNotification";
export const DAILY_NOTIFICATION_ANDROID_ICON = "ic_notification";

// 앱의 core:navigation ScreenDeepLink와 같은 키와 주소여야 앱이 알아보고 화면을 연다.
export const DEEP_LINK_DATA_KEY = "deepLink";

// 알림이 발생한 날을 하루 형식으로 여는 시간표 주소다. localDate는 기기 시간대의 YYYY-MM-DD 날짜다.
export function buildTimetableDeepLink(localDate: string): string {
  return `diary://timetable?type=day&date=${localDate}`;
}

export type RoutineCycle =
  | "FREQ=DAILY"
  | "FREQ=DAILY;BYDAY=MO,TU,WE,TH,FR"
  | "FREQ=DAILY;BYDAY=SA,SU"
  | "FREQ=WEEKLY;WKST=SU"
  | "FREQ=MONTHLY";

export interface DailyRoutine {
  title: string;
  cycle: RoutineCycle;
  unit: string;
  goalAmount: number | null;
  periodTotal: number;
}

// 기준일 당일은 baseDay, 기념일은 간격의 단위와 기준일에서 지난 수다.
export type DDayLabel =
  | { kind: "baseDay" }
  | { kind: "years"; value: number }
  | { kind: "months"; value: number }
  | { kind: "days"; value: number };

export interface DailyDDay {
  title: string;
  label: DDayLabel;
}

export type DDayRepeat = { days: number } | { months: number };
export type DDayOverflowPolicy = "clamp_to_month_end" | "skip" | "next_month_first_day";

export interface DDayCandidate {
  title: string;
  baseDate: string;
  countsBaseDay: boolean;
  repeats: DDayRepeat[];
  endDate: string | null;
  overflowPolicy: DDayOverflowPolicy;
}

// 종류마다 조회에 실패하면 null이다.
export interface DailyNotificationItems {
  memoTitles: string[] | null;
  routines: DailyRoutine[] | null;
  ddays: DailyDDay[] | null;
}

export interface NotificationText {
  title: string;
  body?: string;
}

type Language = "ko" | "en";
type Kind = "memo" | "routine" | "dday";

const KINDS: Kind[] = ["memo", "routine", "dday"];

interface Strings {
  listedTitle: (summary: string[]) => string;
  nothingTitle: string;
  checkTitle: string;
  count: Record<Kind, (count: number) => string>;
  header: Record<Kind, string>;
  check: Record<Kind, string>;
  cycleName: Record<"daily" | "weekly" | "monthly", string>;
  ddayLabel: (label: DDayLabel) => string;
}

const plural = (count: number, one: string, other: string) => `${count} ${count === 1 ? one : other}`;

const STRINGS: Record<Language, Strings> = {
  ko: {
    listedTitle: (summary) => `오늘 ${summary.join(", ")}가 있어요`,
    nothingTitle: "오늘 확인할 일이 없어요",
    checkTitle: "오늘 일정을 확인하세요",
    count: {
      memo: (count) => `메모 ${count}개`,
      routine: (count) => `루틴 ${count}개`,
      dday: (count) => `디데이 ${count}개`,
    },
    header: { memo: "[메모]", routine: "[루틴]", dday: "[디데이]" },
    check: {
      memo: "앱에서 오늘의 메모를 확인하세요",
      routine: "앱에서 오늘의 루틴을 확인하세요",
      dday: "앱에서 오늘의 디데이를 확인하세요",
    },
    cycleName: { daily: "오늘", weekly: "이번 주", monthly: "이번 달" },
    ddayLabel: (label) => {
      switch (label.kind) {
        case "baseDay":
          return "D-Day";
        case "years":
          return `${label.value}주년`;
        case "months":
          return `${label.value}개월`;
        case "days":
          return `${label.value}일`;
      }
    },
  },
  en: {
    listedTitle: (summary) => `Today: ${summary.join(", ")}`,
    nothingTitle: "Nothing to check today",
    checkTitle: "Check today's schedule",
    count: {
      memo: (count) => plural(count, "memo", "memos"),
      routine: (count) => plural(count, "routine", "routines"),
      dday: (count) => plural(count, "D-day", "D-days"),
    },
    header: { memo: "[Memos]", routine: "[Routines]", dday: "[D-days]" },
    check: {
      memo: "Check today's memos in the app",
      routine: "Check today's routines in the app",
      dday: "Check today's D-days in the app",
    },
    cycleName: { daily: "Today", weekly: "This week", monthly: "This month" },
    ddayLabel: (label) => {
      switch (label.kind) {
        case "baseDay":
          return "D-Day";
        case "years":
          return plural(label.value, "year", "years");
        case "months":
          return plural(label.value, "month", "months");
        case "days":
          return plural(label.value, "day", "days");
      }
    },
  },
};

export function resolveLanguage(languageTag: string): Language {
  return /^ko(?:[-_]|$)/i.test(languageTag) ? "ko" : "en";
}

function cycleName(strings: Strings, cycle: RoutineCycle): string {
  switch (cycle) {
    case "FREQ=WEEKLY;WKST=SU":
      return strings.cycleName.weekly;
    case "FREQ=MONTHLY":
      return strings.cycleName.monthly;
    default:
      return strings.cycleName.daily;
  }
}

// 양은 숫자 바로 뒤에 단위를 띄우지 않고 붙이며, 자릿수를 나누는 기호를 넣지 않는다.
function routineProgress(strings: Strings, routine: DailyRoutine): string {
  const name = cycleName(strings, routine.cycle);
  return routine.goalAmount === null
    ? `${name} ${routine.periodTotal}${routine.unit}`
    : `${name} ${routine.periodTotal} / ${routine.goalAmount}${routine.unit}`;
}

function lines(strings: Strings, kind: Kind, items: DailyNotificationItems): string[] | null {
  switch (kind) {
    case "memo":
      return items.memoTitles?.map((title) => `- ${title}`) ?? null;
    case "routine":
      return items.routines?.map((routine) => `- ${routine.title} · ${routineProgress(strings, routine)}`) ?? null;
    case "dday":
      return items.ddays?.map((dday) => `- ${dday.title} · ${strings.ddayLabel(dday.label)}`) ?? null;
  }
}

// 목록인 종류는 제목의 요약과 본문 묶음에, 확인 안내인 종류는 본문 묶음에만 놓는다. 없음인 종류는 어디에도 놓지 않는다.
// 세 종류가 모두 확인 안내이면 제목만으로 안내하고 본문을 두지 않는다.
export function buildNotificationText(languageTag: string, items: DailyNotificationItems): NotificationText {
  const strings = STRINGS[resolveLanguage(languageTag)];
  const summary: string[] = [];
  const body: string[] = [];
  let failedCount = 0;

  for (const kind of KINDS) {
    const kindLines = lines(strings, kind, items);
    if (kindLines === null) {
      failedCount++;
      body.push(strings.header[kind], strings.check[kind]);
    } else if (kindLines.length > 0) {
      summary.push(strings.count[kind](kindLines.length));
      body.push(strings.header[kind], ...kindLines);
    }
  }

  if (summary.length > 0) {
    return { title: strings.listedTitle(summary), body: body.join("\n") };
  }
  if (failedCount === 0) {
    return { title: strings.nothingTitle };
  }
  if (failedCount === KINDS.length) {
    return { title: strings.checkTitle };
  }
  return { title: strings.checkTitle, body: body.join("\n") };
}

// 날짜는 시간대 없는 YYYY-MM-DD 값이므로 UTC 자정의 일 수로 바꿔 계산한다.
const DAY_MILLIS = 24 * 60 * 60 * 1000;

interface YearMonthDay {
  year: number;
  month: number;
  day: number;
}

function parseDate(date: string): YearMonthDay {
  const [year, month, day] = date.split("-").map(Number);
  return { year, month, day };
}

function epochDay(date: YearMonthDay): number {
  return Date.UTC(date.year, date.month - 1, date.day) / DAY_MILLIS;
}

function daysInMonth(year: number, month: number): number {
  return new Date(Date.UTC(year, month, 0)).getUTCDate();
}

function monthIndex(date: YearMonthDay): number {
  return date.year * 12 + (date.month - 1);
}

// 기준일에서 totalMonths개월 뒤의 기념일 날짜다. 그 달에 기준일과 같은 일이 없으면 달력에 없는 날짜 처리를 따르고, 표시하지 않으면 null이다.
function monthlyAnniversary(base: YearMonthDay, totalMonths: number, policy: DDayOverflowPolicy): number | null {
  const index = monthIndex(base) + totalMonths;
  const year = Math.floor(index / 12);
  const month = (index % 12) + 1;
  const lastDay = daysInMonth(year, month);

  if (base.day <= lastDay) return epochDay({ year, month, day: base.day });
  switch (policy) {
    case "clamp_to_month_end":
      return epochDay({ year, month, day: lastDay });
    case "skip":
      return null;
    case "next_month_first_day":
      return epochDay({ year, month, day: lastDay }) + 1;
  }
}

const LABEL_PRIORITY: Record<DDayLabel["kind"], number> = { baseDay: 0, years: 1, months: 2, days: 3 };

// 그 날이 기준일 당일이면 D-Day, 기념일이면 기념일 문구, 둘 다 아니면 null이다.
// 같은 날짜에 여러 기념일이 걸리면 기준일 당일, 주년, 개월, 일 순으로 앞선 하나를 쓴다.
export function resolveDDayLabel(candidate: DDayCandidate, targetDate: string): DDayLabel | null {
  const base = parseDate(candidate.baseDate);
  const target = parseDate(targetDate);
  const baseDay = epochDay(base);
  const targetDay = epochDay(target);

  if (targetDay < baseDay) return null;
  if (targetDay === baseDay) return { kind: "baseDay" };

  const endDay = candidate.endDate === null ? null : epochDay(parseDate(candidate.endDate));
  if (endDay !== null && targetDay > endDay) return null;

  const labels: DDayLabel[] = [];
  const elapsedDays = targetDay - baseDay + (candidate.countsBaseDay ? 1 : 0);
  const monthsBetween = monthIndex(target) - monthIndex(base);

  for (const repeat of candidate.repeats) {
    if ("days" in repeat) {
      if (elapsedDays % repeat.days === 0) labels.push({ kind: "days", value: elapsedDays });
      continue;
    }

    // 기념일은 그 달 안에 놓이거나, 다음 달 1일로 미뤄지면 다음 달 1일에 놓인다.
    for (const totalMonths of [monthsBetween, monthsBetween - 1]) {
      if (totalMonths <= 0 || totalMonths % repeat.months !== 0) continue;
      if (monthlyAnniversary(base, totalMonths, candidate.overflowPolicy) !== targetDay) continue;
      labels.push(
        repeat.months % 12 === 0 ? { kind: "years", value: totalMonths / 12 } : { kind: "months", value: totalMonths },
      );
    }
  }

  if (labels.length === 0) return null;
  return labels.reduce((best, label) => (LABEL_PRIORITY[label.kind] < LABEL_PRIORITY[best.kind] ? label : best));
}

export function toDailyDDays(candidates: DDayCandidate[], targetDate: string): DailyDDay[] {
  return candidates.flatMap((candidate) => {
    const label = resolveDDayLabel(candidate, targetDate);
    return label === null ? [] : [{ title: candidate.title, label }];
  });
}

export function buildFcmMessage(
  target: FcmTarget,
  text: NotificationText,
  localDate: string,
  expiresAt: Date,
  now: Date,
): FcmMessage {
  const ttlSeconds = Math.max(0, Math.floor((expiresAt.getTime() - now.getTime()) / 1000));

  return {
    ...target,
    notification: text.body === undefined ? { title: text.title } : { title: text.title, body: text.body },
    data: { [DEEP_LINK_DATA_KEY]: buildTimetableDeepLink(localDate) },
    android: {
      collapse_key: DAILY_NOTIFICATION_COLLAPSE_ID,
      ttl: `${ttlSeconds}s`,
      notification: {
        channel_id: DAILY_NOTIFICATION_ANDROID_CHANNEL_ID,
        tag: DAILY_NOTIFICATION_COLLAPSE_ID,
        icon: DAILY_NOTIFICATION_ANDROID_ICON,
      },
    },
    apns: {
      headers: {
        "apns-push-type": "alert",
        "apns-collapse-id": DAILY_NOTIFICATION_COLLAPSE_ID,
        "apns-expiration": `${Math.floor(expiresAt.getTime() / 1000)}`,
      },
    },
  };
}
