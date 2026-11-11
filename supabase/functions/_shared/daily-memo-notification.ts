import type { FcmMessage, FcmTarget } from "./fcm.ts";

export const DAILY_MEMO_NOTIFICATION_COLLAPSE_ID = "daily-memo";
export const DAILY_MEMO_NOTIFICATION_ANDROID_CHANNEL_ID = "dailyMemo";
export const DAILY_MEMO_NOTIFICATION_ANDROID_ICON = "ic_notification";

// 앱의 core:navigation ScreenDeepLink와 같은 키와 주소여야 앱이 알아보고 화면을 연다.
export const DEEP_LINK_DATA_KEY = "deepLink";

// 알림이 발생한 날을 하루 형식으로 여는 시간표 주소다. localDate는 기기 시간대의 YYYY-MM-DD 날짜다.
export function buildTimetableDeepLink(localDate: string): string {
  return `diary://timetable?type=day&date=${localDate}`;
}

export type DailyMemoNotificationContent =
  | { kind: "listed"; titles: string[] }
  | { kind: "empty" }
  | { kind: "unavailable" };

export interface NotificationText {
  title: string;
  body?: string;
}

type Language = "ko" | "en";

const TITLES: Record<Language, { unavailable: string; empty: string; listed: (count: number) => string }> = {
  ko: {
    unavailable: "오늘의 메모를 확인하세요",
    empty: "오늘 확인할 메모가 없어요",
    listed: (count) => `오늘 확인할 메모가 ${count}개 있어요`,
  },
  en: {
    unavailable: "Check today's memos",
    empty: "No memos to check today",
    listed: (count) => (count === 1 ? "You have 1 memo to check today" : `You have ${count} memos to check today`),
  },
};

export function resolveLanguage(languageTag: string): Language {
  return /^ko(?:[-_]|$)/i.test(languageTag) ? "ko" : "en";
}

export function toContent(titles: string[] | null): DailyMemoNotificationContent {
  if (titles === null) return { kind: "unavailable" };
  if (titles.length === 0) return { kind: "empty" };
  return { kind: "listed", titles };
}

export function buildNotificationText(languageTag: string, content: DailyMemoNotificationContent): NotificationText {
  const titles = TITLES[resolveLanguage(languageTag)];

  switch (content.kind) {
    case "unavailable":
      return { title: titles.unavailable };
    case "empty":
      return { title: titles.empty };
    case "listed":
      return {
        title: titles.listed(content.titles.length),
        body: content.titles.map((title) => `- ${title}`).join("\n"),
      };
  }
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
      collapse_key: DAILY_MEMO_NOTIFICATION_COLLAPSE_ID,
      ttl: `${ttlSeconds}s`,
      notification: {
        channel_id: DAILY_MEMO_NOTIFICATION_ANDROID_CHANNEL_ID,
        tag: DAILY_MEMO_NOTIFICATION_COLLAPSE_ID,
        icon: DAILY_MEMO_NOTIFICATION_ANDROID_ICON,
      },
    },
    apns: {
      headers: {
        "apns-push-type": "alert",
        "apns-collapse-id": DAILY_MEMO_NOTIFICATION_COLLAPSE_ID,
        "apns-expiration": `${Math.floor(expiresAt.getTime() / 1000)}`,
      },
    },
  };
}
