import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { HttpError, servePost } from "../_shared/http.ts";
import { createUserClient } from "../_shared/supabase.ts";
import { contactTagPushRequestSchema } from "../_shared/sync.ts";

servePost(contactTagPushRequestSchema, async ({ contactTagList }, request) => {
  const client = createUserClient(request);
  const { data, error } = await client.rpc("push_contact_tags", { items: contactTagList });

  if (error) {
    console.error("push_contact_tags failed", error);
    throw new HttpError(500, "sync_push_failed");
  }

  return { affectedCount: data };
});
