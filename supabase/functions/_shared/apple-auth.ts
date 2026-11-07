import { importPKCS8, SignJWT } from "npm:jose@5";
import { HttpError } from "./http.ts";

const APPLE_AUDIENCE = "https://appleid.apple.com";
const APPLE_TOKEN_ENDPOINT = "https://appleid.apple.com/auth/token";
const APPLE_REVOKE_ENDPOINT = "https://appleid.apple.com/auth/revoke";

// Apple은 client secret의 유효 기간을 6개월까지 허용하지만, 요청마다 새로 만들므로 짧게 둔다.
const CLIENT_SECRET_LIFETIME_SECONDS = 300;

// 기기의 Apple 계정 로그인(iOS)이 발급한 코드는 복귀 주소 없이, Apple 웹 로그인이 발급한 코드는
// 로그인을 요청할 때와 같은 복귀 주소를 함께 보내야 바꿀 수 있어 두 목록을 나눈다.
const NATIVE_CLIENT_IDS = readList("APPLE_NATIVE_CLIENT_IDS");
const WEB_CLIENT_IDS = readList("APPLE_WEB_CLIENT_IDS");

export interface AppleToken {
  token: string;
  tokenTypeHint: "refresh_token" | "access_token";
  subject: string;
}

export function isKnownAppleClientId(clientId: string): boolean {
  return NATIVE_CLIENT_IDS.has(clientId) || WEB_CLIENT_IDS.has(clientId);
}

export async function exchangeAppleAuthorizationCode(code: string, clientId: string): Promise<AppleToken> {
  const parameters = new URLSearchParams({
    client_id: clientId,
    client_secret: await createClientSecret(clientId),
    code,
    grant_type: "authorization_code",
  });
  if (WEB_CLIENT_IDS.has(clientId)) {
    parameters.set("redirect_uri", appleCallbackUrl());
  }

  const response = await fetch(APPLE_TOKEN_ENDPOINT, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: parameters,
  });

  const body = await response.json().catch(() => null);
  const subject = typeof body?.id_token === "string" ? readSubject(body.id_token, clientId) : null;
  if (!response.ok || !subject || (!body?.refresh_token && !body?.access_token)) {
    console.error("apple token exchange failed", response.status, body?.error);
    throw new HttpError(401, "apple_token_exchange_failed");
  }

  return body.refresh_token
    ? { token: body.refresh_token, tokenTypeHint: "refresh_token", subject }
    : { token: body.access_token, tokenTypeHint: "access_token", subject };
}

export async function revokeAppleToken(token: AppleToken, clientId: string): Promise<void> {
  const response = await fetch(APPLE_REVOKE_ENDPOINT, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      client_id: clientId,
      client_secret: await createClientSecret(clientId),
      token: token.token,
      token_type_hint: token.tokenTypeHint,
    }),
  });

  if (!response.ok) {
    console.error("apple token revoke failed", response.status, await response.text().catch(() => ""));
    throw new HttpError(502, "apple_token_revoke_failed");
  }
}

async function createClientSecret(clientId: string): Promise<string> {
  const teamId = Deno.env.get("APPLE_TEAM_ID");
  const keyId = Deno.env.get("APPLE_KEY_ID");
  const privateKey = Deno.env.get("APPLE_PRIVATE_KEY");

  if (!teamId || !keyId || !privateKey) {
    console.error("apple client secret is not configured");
    throw new HttpError(500, "internal_error");
  }

  const key = await importPKCS8(privateKey.replace(/\\n/g, "\n"), "ES256");
  const issuedAt = Math.floor(Date.now() / 1000);

  return new SignJWT({})
    .setProtectedHeader({ alg: "ES256", kid: keyId })
    .setIssuer(teamId)
    .setIssuedAt(issuedAt)
    .setExpirationTime(issuedAt + CLIENT_SECRET_LIFETIME_SECONDS)
    .setAudience(APPLE_AUDIENCE)
    .setSubject(clientId)
    .sign(key);
}

// 토큰 응답은 Apple과 직접 맺은 보안 연결로 받으므로 신원 증명의 서명은 다시 확인하지 않고 내용만 읽는다.
function readSubject(idToken: string, clientId: string): string | null {
  try {
    const payload = JSON.parse(new TextDecoder().decode(decodeBase64Url(idToken.split(".")[1] ?? "")));
    if (payload?.aud !== clientId || typeof payload?.sub !== "string") return null;
    return payload.sub;
  } catch {
    return null;
  }
}

function decodeBase64Url(value: string): Uint8Array {
  const base64 = value.replace(/-/g, "+").replace(/_/g, "/").padEnd(Math.ceil(value.length / 4) * 4, "=");
  return Uint8Array.from(atob(base64), (character) => character.charCodeAt(0));
}

function appleCallbackUrl(): string {
  return `${Deno.env.get("SUPABASE_URL")}/functions/v1/v1-session-apple-callback`;
}

function readList(name: string): Set<string> {
  return new Set(
    (Deno.env.get(name) ?? "")
      .split(",")
      .map((value) => value.trim())
      .filter((value) => value.length > 0),
  );
}
