import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { HttpError, servePost } from "../_shared/http.ts";
import { createUserClient } from "../_shared/supabase.ts";
import { routineRecordPushRequestSchema } from "../_shared/sync.ts";

servePost(routineRecordPushRequestSchema, async ({ routineRecordList }, request) => {
  const client = createUserClient(request);
  const { data, error } = await client.rpc("push_routine_records", { items: routineRecordList });

  if (error) {
    console.error("push_routine_records failed", error);
    throw new HttpError(500, "sync_push_failed");
  }

  return { affectedCount: data };
});
