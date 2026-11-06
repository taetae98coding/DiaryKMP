import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { HttpError, servePost } from "../_shared/http.ts";
import { createUserClient } from "../_shared/supabase.ts";
import { qrTagPushRequestSchema } from "../_shared/sync.ts";

servePost(qrTagPushRequestSchema, async ({ qrTagList }, request) => {
  const client = createUserClient(request);
  const { data, error } = await client.rpc("push_qr_tags", { items: qrTagList });

  if (error) {
    console.error("push_qr_tags failed", error);
    throw new HttpError(500, "sync_push_failed");
  }

  return { affectedCount: data };
});
