import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { HttpError, servePost } from "../_shared/http.ts";
import { createUserClient } from "../_shared/supabase.ts";
import { tagLinkPushRequestSchema } from "../_shared/sync.ts";

servePost(tagLinkPushRequestSchema, async ({ tagLinkList }, request) => {
  const client = createUserClient(request);
  const { data, error } = await client.rpc("push_tag_links", { items: tagLinkList });

  if (error) {
    console.error("push_tag_links failed", error);
    throw new HttpError(500, "sync_push_failed");
  }

  return { affectedCount: data };
});
