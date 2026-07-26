import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { HttpError, servePost } from "../_shared/http.ts";
import { createUserClient } from "../_shared/supabase.ts";
import { memoPlacePushRequestSchema } from "../_shared/sync.ts";

servePost(memoPlacePushRequestSchema, async ({ memoPlaceList }, request) => {
  const client = createUserClient(request);
  const { data, error } = await client.rpc("push_memo_places", { items: memoPlaceList });

  if (error) {
    console.error("push_memo_places failed", error);
    throw new HttpError(500, "sync_push_failed");
  }

  return { affectedCount: data };
});
