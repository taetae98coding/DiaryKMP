import { createClient } from "jsr:@supabase/supabase-js@2";
import { HttpError } from "./http.ts";

const SUPABASE_URL = Deno.env.get("SUPABASE_URL")!;
const SUPABASE_ANON_KEY = Deno.env.get("SUPABASE_ANON_KEY")!;

export type IdTokenProvider = "google" | "apple";

export interface Session {
  accessToken: string;
  refreshToken: string;
}

export async function issueSessionFromIdToken(
  provider: IdTokenProvider,
  idToken: string,
  nonce?: string,
): Promise<Session> {
  const supabase = createClient(SUPABASE_URL, SUPABASE_ANON_KEY, {
    auth: { persistSession: false, autoRefreshToken: false },
  });

  const { data, error } = await supabase.auth.signInWithIdToken({
    provider,
    token: idToken,
    nonce,
  });

  if (error || !data.session) {
    console.error("signInWithIdToken failed", provider, error);
    throw new HttpError(401, error?.message ?? "sign_in_failed");
  }

  return {
    accessToken: data.session.access_token,
    refreshToken: data.session.refresh_token,
  };
}
