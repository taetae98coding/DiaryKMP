import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { HttpError, servePost } from "../_shared/http.ts";
import { createUserClient } from "../_shared/supabase.ts";
import { routinePushRequestSchema } from "../_shared/sync.ts";

servePost(routinePushRequestSchema, async ({ routineList }, request) => {
  const client = createUserClient(request);
  const { data, error } = await client.rpc("push_routines", { items: routineList });

  if (error) {
    console.error("push_routines failed", error);
    throw new HttpError(500, "sync_push_failed");
  }

  return { affectedCount: data };
});
