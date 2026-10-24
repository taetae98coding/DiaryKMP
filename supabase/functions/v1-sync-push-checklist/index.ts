import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { HttpError, servePost } from "../_shared/http.ts";
import { createUserClient } from "../_shared/supabase.ts";
import { checklistPushRequestSchema } from "../_shared/sync.ts";

servePost(checklistPushRequestSchema, async ({ checklistList }, request) => {
  const client = createUserClient(request);
  const { data, error } = await client.rpc("push_checklists", { items: checklistList });

  if (error) {
    console.error("push_checklists failed", error);
    throw new HttpError(500, "sync_push_failed");
  }

  return { affectedCount: data };
});
