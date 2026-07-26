import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { HttpError, servePost } from "../_shared/http.ts";
import { createUserClient } from "../_shared/supabase.ts";
import { placePushRequestSchema } from "../_shared/sync.ts";

servePost(placePushRequestSchema, async ({ placeList }, request) => {
  const client = createUserClient(request);
  const { data, error } = await client.rpc("push_places", { items: placeList });

  if (error) {
    console.error("push_places failed", error);
    throw new HttpError(500, "sync_push_failed");
  }

  return { affectedCount: data };
});
