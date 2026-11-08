import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { HttpError, servePost } from "../_shared/http.ts";
import { createUserClient } from "../_shared/supabase.ts";
import { ddayTagPushRequestSchema } from "../_shared/sync.ts";

servePost(ddayTagPushRequestSchema, async ({ ddayTagList }, request) => {
  const client = createUserClient(request);
  const { data, error } = await client.rpc("push_dday_tags", { items: ddayTagList });

  if (error) {
    console.error("push_dday_tags failed", error);
    throw new HttpError(500, "sync_push_failed");
  }

  return { affectedCount: data };
});
