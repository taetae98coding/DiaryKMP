import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { HttpError, servePost } from "../_shared/http.ts";
import { createUserClient } from "../_shared/supabase.ts";
import { webTagPushRequestSchema } from "../_shared/sync.ts";

servePost(webTagPushRequestSchema, async ({ webTagList }, request) => {
  const client = createUserClient(request);
  const { data, error } = await client.rpc("push_web_tags", { items: webTagList });

  if (error) {
    console.error("push_web_tags failed", error);
    throw new HttpError(500, "sync_push_failed");
  }

  return { affectedCount: data };
});
