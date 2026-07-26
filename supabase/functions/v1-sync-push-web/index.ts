import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { HttpError, servePost } from "../_shared/http.ts";
import { createUserClient } from "../_shared/supabase.ts";
import { webPushRequestSchema } from "../_shared/sync.ts";

servePost(webPushRequestSchema, async ({ webList }, request) => {
  const client = createUserClient(request);
  const { data, error } = await client.rpc("push_webs", { items: webList });

  if (error) {
    console.error("push_webs failed", error);
    throw new HttpError(500, "sync_push_failed");
  }

  return { affectedCount: data };
});
