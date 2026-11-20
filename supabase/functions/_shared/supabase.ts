import { createClient, type SupabaseClient } from "npm:@supabase/supabase-js@2";
import { HttpError } from "./http.ts";

export function createUserClient(request: Request) {
  const url = Deno.env.get("SUPABASE_URL");
  const apiKey = request.headers.get("apikey");
  const authorization = request.headers.get("Authorization");

  if (!url || !apiKey || !authorization) {
    throw new HttpError(401, "unauthorized");
  }

  return createClient(url, apiKey, {
    auth: {
      autoRefreshToken: false,
      detectSessionInUrl: false,
      persistSession: false,
    },
    global: {
      headers: { Authorization: authorization },
    },
  });
}

// 세션 없이 프로젝트 키만으로 부르는 요청용이다. 게스트 앱은 세션이 없어 Authorization 없이 apikey만 보낸다.
export function createPublicClient(request: Request) {
  const url = Deno.env.get("SUPABASE_URL");
  const apiKey = request.headers.get("apikey");

  if (!url || !apiKey) {
    throw new HttpError(401, "unauthorized");
  }

  return createClient(url, apiKey, {
    auth: {
      autoRefreshToken: false,
      detectSessionInUrl: false,
      persistSession: false,
    },
  });
}

export async function requireUserId(client: SupabaseClient, request: Request): Promise<string> {
  const accessToken = request.headers.get("Authorization")?.replace(/^Bearer\s+/i, "");

  if (!accessToken) {
    throw new HttpError(401, "unauthorized");
  }

  const { data, error } = await client.auth.getUser(accessToken);

  if (error || !data.user) {
    console.error("getUser failed", error);
    throw new HttpError(401, "unauthorized");
  }

  return data.user.id;
}

// 프로젝트 API 키는 이름별 JSON으로 주입되고, 프로젝트에 처음 만들어지는 키의 이름이 default다.
function readDefaultApiKey(name: "SUPABASE_PUBLISHABLE_KEYS" | "SUPABASE_SECRET_KEYS"): string {
  let key: unknown;
  try {
    key = JSON.parse(Deno.env.get(name) ?? "{}").default;
  } catch (error) {
    console.error(`${name} is not a JSON object`, error);
  }

  if (typeof key !== "string" || key === "") {
    console.error(`${name} has no default key`);
    throw new HttpError(500, "internal_error");
  }

  return key;
}

export function publishableKey(): string {
  return readDefaultApiKey("SUPABASE_PUBLISHABLE_KEYS");
}

export function secretKey(): string {
  return readDefaultApiKey("SUPABASE_SECRET_KEYS");
}

// 프로필 이미지는 앱이 인증 사용자 정보에서 읽으므로 사용자 메타데이터도 함께 바꿔야 하는데,
// 사용자 자신의 토큰으로는 메타데이터를 바꿀 수 없어 secret 키 클라이언트를 쓴다.
export function createAdminClient(): SupabaseClient {
  const url = Deno.env.get("SUPABASE_URL");

  if (!url) {
    throw new HttpError(500, "internal_error");
  }

  return createClient(url, secretKey(), {
    auth: {
      autoRefreshToken: false,
      detectSessionInUrl: false,
      persistSession: false,
    },
  });
}
