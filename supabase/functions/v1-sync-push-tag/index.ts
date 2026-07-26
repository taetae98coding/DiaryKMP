import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { HttpError, servePost } from "../_shared/http.ts";
import { createUserClient } from "../_shared/supabase.ts";
import { tagPushRequestSchema } from "../_shared/sync.ts";

servePost(tagPushRequestSchema, async ({ tagList }, request) => {
  const client = createUserClient(request);
  const { data, error } = await client.rpc("push_tags", { items: tagList });

  if (error) {
    console.error("push_tags failed", error);
    throw new HttpError(500, "sync_push_failed");
  }

  return { affectedCount: data };
});
