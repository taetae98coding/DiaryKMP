// FCM HTTP v1로 알림을 보낸다. 인증은 Firebase 서비스 계정 키로 서명한 JWT를 Google OAuth 토큰으로 바꿔 쓴다.
// 서비스 계정 키 JSON 전체를 FCM_SERVICE_ACCOUNT 비밀로 넣어야 한다.

const OAUTH_TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";
const FCM_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
const ACCESS_TOKEN_LIFETIME_SECONDS = 3600;

interface ServiceAccount {
  project_id: string;
  client_email: string;
  private_key: string;
}

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
    const raw = Deno.env.get("FCM_SERVICE_ACCOUNT");
    if (!raw) {
      throw new Error("FCM_SERVICE_ACCOUNT is not configured");
    }

    const account = JSON.parse(raw) as ServiceAccount;
    const accessToken = await fetchAccessToken(account);

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

async function fetchAccessToken(account: ServiceAccount): Promise<string> {
  const now = Math.floor(Date.now() / 1000);
  const assertion = await signJwt(
    {
      iss: account.client_email,
      scope: FCM_SCOPE,
      aud: OAUTH_TOKEN_ENDPOINT,
      iat: now,
      exp: now + ACCESS_TOKEN_LIFETIME_SECONDS,
    },
    account.private_key,
  );

  const response = await fetch(OAUTH_TOKEN_ENDPOINT, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      grant_type: "urn:ietf:params:oauth:grant-type:jwt-bearer",
      assertion,
    }),
  });

  const body = await response.json().catch(() => null);
  if (!response.ok || !body?.access_token) {
    console.error("fcm access token request failed", body);
    throw new Error("fcm_access_token_failed");
  }

  return body.access_token;
}

async function signJwt(payload: Record<string, unknown>, privateKeyPem: string): Promise<string> {
  const header = base64UrlEncode(new TextEncoder().encode(JSON.stringify({ alg: "RS256", typ: "JWT" })));
  const claims = base64UrlEncode(new TextEncoder().encode(JSON.stringify(payload)));
  const signingInput = `${header}.${claims}`;

  const key = await crypto.subtle.importKey(
    "pkcs8",
    pemToDer(privateKeyPem),
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false,
    ["sign"],
  );
  const signature = await crypto.subtle.sign("RSASSA-PKCS1-v1_5", key, new TextEncoder().encode(signingInput));

  return `${signingInput}.${base64UrlEncode(new Uint8Array(signature))}`;
}

function pemToDer(pem: string): ArrayBuffer {
  const base64 = pem
    .replace(/-----BEGIN PRIVATE KEY-----/, "")
    .replace(/-----END PRIVATE KEY-----/, "")
    .replace(/\s+/g, "");
  const binary = atob(base64);
  const bytes = new Uint8Array(binary.length);
  for (let index = 0; index < binary.length; index++) {
    bytes[index] = binary.charCodeAt(index);
  }
  return bytes.buffer;
}

function base64UrlEncode(bytes: Uint8Array): string {
  let binary = "";
  for (const byte of bytes) {
    binary += String.fromCharCode(byte);
  }
  return btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}
