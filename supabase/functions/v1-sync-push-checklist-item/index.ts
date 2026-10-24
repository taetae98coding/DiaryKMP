import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { HttpError, servePost } from "../_shared/http.ts";
import { createUserClient } from "../_shared/supabase.ts";
import { checklistItemPushRequestSchema } from "../_shared/sync.ts";

servePost(checklistItemPushRequestSchema, async ({ checklistItemList }, request) => {
  const client = createUserClient(request);
  const { data, error } = await client.rpc("push_checklist_items", { items: checklistItemList });

  if (error) {
    console.error("push_checklist_items failed", error);
    throw new HttpError(500, "sync_push_failed");
  }

  return { affectedCount: data };
});
