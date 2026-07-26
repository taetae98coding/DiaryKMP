import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { HttpError, servePost } from "../_shared/http.ts";
import { createUserClient } from "../_shared/supabase.ts";
import { placeTagPushRequestSchema } from "../_shared/sync.ts";

servePost(placeTagPushRequestSchema, async ({ placeTagList }, request) => {
  const client = createUserClient(request);
  const { data, error } = await client.rpc("push_place_tags", { items: placeTagList });

  if (error) {
    console.error("push_place_tags failed", error);
    throw new HttpError(500, "sync_push_failed");
  }

  return { affectedCount: data };
});
