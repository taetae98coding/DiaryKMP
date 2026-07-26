import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { HttpError, servePost } from "../_shared/http.ts";
import { createUserClient } from "../_shared/supabase.ts";
import { contactPushRequestSchema } from "../_shared/sync.ts";

servePost(contactPushRequestSchema, async ({ contactList }, request) => {
  const client = createUserClient(request);
  const { data, error } = await client.rpc("push_contacts", {
    items: contactList,
  });

  if (error) {
    console.error("push_contacts failed", error);
    throw new HttpError(500, "sync_push_failed");
  }

  return { affectedCount: data };
});
