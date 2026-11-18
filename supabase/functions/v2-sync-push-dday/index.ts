import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { HttpError, servePost } from "../_shared/http.ts";
import { createUserClient } from "../_shared/supabase.ts";
import { ddayPushRequestSchemaV2 } from "../_shared/sync.ts";

servePost(ddayPushRequestSchemaV2, async ({ ddayList }, request) => {
  const client = createUserClient(request);
  const { data, error } = await client.rpc("push_ddays_v2", { items: ddayList });

  if (error) {
    console.error("push_ddays_v2 failed", error);
    throw new HttpError(500, "sync_push_failed");
  }

  return { affectedCount: data };
});
