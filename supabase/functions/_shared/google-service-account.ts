// Firebase 서비스 계정 키로 서명한 JWT를 Google OAuth 토큰으로 바꾼다.
// 서비스 계정 키 JSON 전체를 FCM_SERVICE_ACCOUNT 비밀로 넣어야 한다.

const OAUTH_TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";
const ACCESS_TOKEN_LIFETIME_SECONDS = 3600;

export interface ServiceAccount {
  project_id: string;
  client_email: string;
  private_key: string;
}

export function readFirebaseServiceAccount(): ServiceAccount {
  const raw = Deno.env.get("FCM_SERVICE_ACCOUNT");
  if (!raw) {
    throw new Error("FCM_SERVICE_ACCOUNT is not configured");
  }

  return JSON.parse(raw) as ServiceAccount;
}

export async function fetchGoogleAccessToken(account: ServiceAccount, scope: string): Promise<string> {
  const now = Math.floor(Date.now() / 1000);
  const assertion = await signJwt(
    {
      iss: account.client_email,
      scope,
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
    console.error("google access token request failed", body);
    throw new Error("google_access_token_failed");
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
