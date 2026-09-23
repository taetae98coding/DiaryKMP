import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { HttpError, servePost } from "../_shared/http.ts";
import { createUserClient } from "../_shared/supabase.ts";
import { memoContactPushRequestSchema } from "../_shared/sync.ts";

servePost(memoContactPushRequestSchema, async ({ memoContactList }, request) => {
  const client = createUserClient(request);
  const { data, error } = await client.rpc("push_memo_contacts", { items: memoContactList });

  if (error) {
    console.error("push_memo_contacts failed", error);
    throw new HttpError(500, "sync_push_failed");
  }

  return { affectedCount: data };
});
