// 일일 메모 알림의 문구와 FCM 메시지 구성. 문구는 docs/design/daily-memo-notification.md와 같은 이름의 표를 따른다.

export const DAILY_MEMO_NOTIFICATION_COLLAPSE_ID = "daily-memo";
export const DAILY_MEMO_NOTIFICATION_ANDROID_CHANNEL_ID = "dailyMemo";
export const DAILY_MEMO_NOTIFICATION_ANDROID_ICON = "ic_notification";

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

export function buildFcmMessage(token: string, text: NotificationText, expiresAt: Date, now: Date) {
  const ttlSeconds = Math.max(0, Math.floor((expiresAt.getTime() - now.getTime()) / 1000));

  return {
    token,
    notification: text.body === undefined ? { title: text.title } : { title: text.title, body: text.body },
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
