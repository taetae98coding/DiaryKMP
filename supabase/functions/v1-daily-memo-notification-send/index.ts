import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { buildFcmMessage, buildNotificationText, toContent } from "../_shared/daily-memo-notification.ts";
import { FcmClient } from "../_shared/fcm.ts";
import { HttpError, serve } from "../_shared/http.ts";
import { createAdminClient } from "../_shared/supabase.ts";

// pg_cron이 15분마다 service role 키로 호출한다. 사용자 세션으로는 호출할 수 없어야 하므로 키가 정확히 일치하는지 본다.
serve(async (request) => {
  const bearer = request.headers.get("Authorization")?.replace(/^Bearer\s+/i, "");
  const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
  if (!serviceRoleKey || bearer !== serviceRoleKey) {
    throw new HttpError(401, "unauthorized");
  }

  const client = createAdminClient();

  const { error: purgeError } = await client.rpc("purge_stale_fcm_tokens");
  if (purgeError) {
    console.error("purge_stale_fcm_tokens failed", purgeError);
  }

  const { data: dueTokens, error: listError } = await client.rpc("list_due_daily_memo_notification_tokens");
  if (listError) {
    console.error("list_due_daily_memo_notification_tokens failed", listError);
    throw new HttpError(500, "due_tokens_unavailable");
  }

  if (dueTokens.length === 0) {
    return { sent: 0, failed: 0, removed: 0 };
  }

  const fcm = await FcmClient.create();
  const now = new Date();
  let sent = 0;
  let failed = 0;
  let removed = 0;

  for (const due of dueTokens) {
    try {
      const { data: memos, error: memoError } = await client.rpc("list_daily_memos", {
        target_account_id: due.account_id,
        target_date: due.local_date,
      });
      if (memoError) {
        console.error("list_daily_memos failed", { account_id: due.account_id, error: memoError });
      }

      const text = buildNotificationText(
        due.language,
        toContent(memoError ? null : memos.map((memo: { title: string }) => memo.title)),
      );
      const result = await fcm.send(buildFcmMessage(due.token, text, new Date(due.expires_at), now));

      switch (result.status) {
        case "sent":
          sent++;
          break;
        case "invalid_token": {
          removed++;
          const { error } = await client.rpc("unregister_fcm_token", { token: due.token });
          if (error) {
            console.error("unregister_fcm_token failed", error);
          }
          break;
        }
        case "failed":
          failed++;
          console.error("fcm send failed", result.error);
          break;
      }
    } catch (error) {
      failed++;
      console.error("daily memo notification failed", { account_id: due.account_id, error });
    }
  }

  return { sent, failed, removed };
});
