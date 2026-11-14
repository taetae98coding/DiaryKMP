import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { HttpError, servePost } from "../_shared/http.ts";
import { createUserClient } from "../_shared/supabase.ts";
import { routineTagPushRequestSchema } from "../_shared/sync.ts";

servePost(routineTagPushRequestSchema, async ({ routineTagList }, request) => {
  const client = createUserClient(request);
  const { data, error } = await client.rpc("push_routine_tags", { items: routineTagList });

  if (error) {
    console.error("push_routine_tags failed", error);
    throw new HttpError(500, "sync_push_failed");
  }

  return { affectedCount: data };
});
