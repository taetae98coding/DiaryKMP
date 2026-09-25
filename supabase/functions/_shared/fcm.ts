import { fetchGoogleAccessToken, readFirebaseServiceAccount } from "./google-service-account.ts";

// FCM HTTP v1로 알림을 보낸다. 인증은 Firebase 서비스 계정 키로 받은 Google OAuth 토큰을 쓴다.

const FCM_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";

export interface FcmMessage {
  token: string;
  notification: { title: string; body?: string };
  android: {
    collapse_key: string;
    ttl: string;
    notification: { channel_id: string; tag: string; icon: string };
  };
  apns: {
    headers: Record<string, string>;
  };
}

export type FcmSendResult =
  | { status: "sent" }
  | { status: "invalid_token" }
  | { status: "failed"; error: unknown };

export class FcmClient {
  private constructor(
    private readonly projectId: string,
    private readonly accessToken: string,
  ) {}

  static async create(): Promise<FcmClient> {
    const account = readFirebaseServiceAccount();
    const accessToken = await fetchGoogleAccessToken(account, FCM_SCOPE);

    return new FcmClient(account.project_id, accessToken);
  }

  async send(message: FcmMessage): Promise<FcmSendResult> {
    const response = await fetch(`https://fcm.googleapis.com/v1/projects/${this.projectId}/messages:send`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${this.accessToken}`,
      },
      body: JSON.stringify({ message }),
    });

    if (response.ok) {
      return { status: "sent" };
    }

    const body = await response.json().catch(() => null);
    if (isInvalidTokenError(response.status, body)) {
      return { status: "invalid_token" };
    }

    return { status: "failed", error: { status: response.status, body } };
  }
}

// FCM은 지워진 토큰을 404 UNREGISTERED로, 형식이 틀린 토큰을 400 INVALID_ARGUMENT로 답한다.
// 둘 다 이 토큰으로는 다시 보낼 수 없다는 뜻이므로 서버에서 지울 대상으로 본다.
function isInvalidTokenError(status: number, body: unknown): boolean {
  const errorCode = fcmErrorCode(body);
  if (status === 404 && errorCode === "UNREGISTERED") return true;
  if (status === 400 && errorCode === "INVALID_ARGUMENT") {
    const message = (body as { error?: { message?: string } })?.error?.message ?? "";
    return message.includes("registration token");
  }
  return false;
}

function fcmErrorCode(body: unknown): string | undefined {
  const details = (body as { error?: { details?: Array<{ errorCode?: string }> } })?.error?.details ?? [];
  return details.find((detail) => detail.errorCode)?.errorCode;
}
