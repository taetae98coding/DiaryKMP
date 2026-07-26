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

// 프로필 이미지는 앱이 인증 사용자 정보에서 읽으므로 사용자 메타데이터도 함께 바꿔야 하는데,
// 사용자 자신의 토큰으로는 메타데이터를 바꿀 수 없어 service role 클라이언트를 쓴다.
export function createAdminClient(): SupabaseClient {
  const url = Deno.env.get("SUPABASE_URL");
  const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");

  if (!url || !serviceRoleKey) {
    throw new HttpError(500, "internal_error");
  }

  return createClient(url, serviceRoleKey, {
    auth: {
      autoRefreshToken: false,
      detectSessionInUrl: false,
      persistSession: false,
    },
  });
}
