import { createClient } from "jsr:@supabase/supabase-js@2";
import { HttpError } from "./http.ts";
import { isAppliedProfileImage } from "./profile-image.ts";
import { createAdminClient } from "./supabase.ts";

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

  await keepAppliedProfileImage(data.session.user.id);

  return {
    accessToken: data.session.access_token,
    refreshToken: data.session.refresh_token,
  };
}

// 로그인은 로그인 수단이 제공한 사용자 정보로 인증 제공자의 사용자 정보를 갱신하므로,
// 사용자가 반영한 프로필 이미지가 있으면 세션을 돌려주기 전에 그 주소로 되돌린다.
// 되돌리지 못해도 계정이 보관한 주소는 남아 다음 로그인에서 다시 되돌릴 수 있으므로 로그인을 실패시키지 않는다.
async function keepAppliedProfileImage(accountId: string): Promise<void> {
  try {
    const admin = createAdminClient();

    const { data: account, error: accountError } = await admin
      .from("account")
      .select("profile_image")
      .eq("id", accountId)
      .maybeSingle();

    if (accountError || !account) {
      console.error("account profile image read failed", accountError);
      return;
    }

    const appliedImageUrl: string | null = account.profile_image;

    if (!isAppliedProfileImage(admin, appliedImageUrl)) return;

    const { data: user, error: userError } = await admin.auth.admin.getUserById(accountId);

    if (userError || !user.user) {
      console.error("getUserById failed", userError);
      return;
    }

    const metadata = user.user.user_metadata ?? {};

    if (metadata.avatar_url === appliedImageUrl) return;

    const { error: updateError } = await admin.auth.admin.updateUserById(accountId, {
      user_metadata: { ...metadata, avatar_url: appliedImageUrl },
    });

    if (updateError) {
      console.error("profile image restore failed", updateError);
    }
  } catch (error) {
    console.error("profile image restore failed", error);
  }
}
