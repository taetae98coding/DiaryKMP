import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { HttpError, servePost } from "../_shared/http.ts";
import { createUserClient } from "../_shared/supabase.ts";
import { checklistTagPushRequestSchema } from "../_shared/sync.ts";

servePost(checklistTagPushRequestSchema, async ({ checklistTagList }, request) => {
  const client = createUserClient(request);
  const { data, error } = await client.rpc("push_checklist_tags", { items: checklistTagList });

  if (error) {
    console.error("push_checklist_tags failed", error);
    throw new HttpError(500, "sync_push_failed");
  }

  return { affectedCount: data };
});
