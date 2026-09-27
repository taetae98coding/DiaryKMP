import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { HttpError, servePost } from "../_shared/http.ts";
import { createUserClient } from "../_shared/supabase.ts";
import { qrPushRequestSchema } from "../_shared/sync.ts";

servePost(qrPushRequestSchema, async ({ qrList }, request) => {
  const client = createUserClient(request);
  const { data, error } = await client.rpc("push_qrs", { items: qrList });

  if (error) {
    console.error("push_qrs failed", error);
    throw new HttpError(500, "sync_push_failed");
  }

  return { affectedCount: data };
});
