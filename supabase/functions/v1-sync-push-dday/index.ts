import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { HttpError, servePost } from "../_shared/http.ts";
import { createUserClient } from "../_shared/supabase.ts";
import { ddayPushRequestSchema } from "../_shared/sync.ts";

servePost(ddayPushRequestSchema, async ({ ddayList }, request) => {
  const client = createUserClient(request);
  const { data, error } = await client.rpc("push_ddays", { items: ddayList });

  if (error) {
    console.error("push_ddays failed", error);
    throw new HttpError(500, "sync_push_failed");
  }

  return { affectedCount: data };
});
