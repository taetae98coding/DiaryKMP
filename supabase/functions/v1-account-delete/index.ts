import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import type { SupabaseClient } from "npm:@supabase/supabase-js@2";
import { z } from "npm:zod@3";
import { type AppleToken, exchangeAppleAuthorizationCode, isKnownAppleClientId, revokeAppleToken } from "../_shared/apple-auth.ts";
import { FILE_BUCKET } from "../_shared/file.ts";
import { HttpError, servePost } from "../_shared/http.ts";
import { PROFILE_IMAGE_BUCKET } from "../_shared/profile-image.ts";
import { createAdminClient, createUserClient, requireUserId } from "../_shared/supabase.ts";

const STORAGE_LIST_LIMIT = 1000;

const requestSchema = z.object({
  apple: z
    .object({
      authorizationCode: z.string().min(1),
      clientId: z.string().min(1),
    })
    .nullish(),
});

servePost(requestSchema, async ({ apple }, request) => {
  const accountId = await requireUserId(createUserClient(request), request);
  const admin = createAdminClient();

  if (apple) {
    if (!isKnownAppleClientId(apple.clientId)) {
      throw new HttpError(400, "unknown_apple_client");
    }

    const token = await exchangeAppleAuthorizationCode(apple.authorizationCode, apple.clientId);
    await requireLinkedAppleUser(admin, accountId, token);
    await revokeAppleToken(token, apple.clientId);
  }

  await removeFolder(admin, PROFILE_IMAGE_BUCKET, accountId);
  await removeFolder(admin, FILE_BUCKET, accountId);

  const { error } = await admin.rpc("delete_account", { account_id: accountId });
  if (error) {
    console.error("delete account failed", error);
    throw new HttpError(500, "account_delete_failed");
  }

  return {};
});

async function requireLinkedAppleUser(admin: SupabaseClient, accountId: string, token: AppleToken): Promise<void> {
  const { data, error } = await admin.auth.admin.getUserById(accountId);
  if (error || !data.user) {
    console.error("read user failed", error);
    throw new HttpError(500, "account_delete_failed");
  }

  const isLinked = (data.user.identities ?? []).some((identity) =>
    identity.provider === "apple" && (identity.identity_data?.sub ?? identity.id) === token.subject
  );
  if (!isLinked) {
    throw new HttpError(403, "apple_user_mismatch");
  }
}

// 지운 뒤 처음부터 다시 목록을 읽어, 지우는 동안 목록의 순서가 바뀌어도 남는 것이 없게 한다.
async function removeFolder(admin: SupabaseClient, bucket: string, accountId: string): Promise<void> {
  while (true) {
    const { data, error } = await admin.storage.from(bucket).list(accountId, { limit: STORAGE_LIST_LIMIT });
    if (error) {
      console.error("storage list failed", bucket, error);
      throw new HttpError(500, "account_delete_failed");
    }
    if (data.length === 0) return;

    const { error: removeError } = await admin.storage.from(bucket).remove(
      data.map((object) => `${accountId}/${object.name}`),
    );
    if (removeError) {
      console.error("storage remove failed", bucket, removeError);
      throw new HttpError(500, "account_delete_failed");
    }
  }
}
