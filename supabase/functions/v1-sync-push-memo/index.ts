import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { HttpError, servePost } from "../_shared/http.ts";
import { createUserClient } from "../_shared/supabase.ts";
import { memoPushRequestSchema } from "../_shared/sync.ts";

servePost(memoPushRequestSchema, async ({ memoList }, request) => {
  const client = createUserClient(request);
  const { data, error } = await client.rpc("push_memos", { items: memoList });

  if (error) {
    console.error("push_memos failed", error);
    throw new HttpError(500, "sync_push_failed");
  }

  return { affectedCount: data };
});
