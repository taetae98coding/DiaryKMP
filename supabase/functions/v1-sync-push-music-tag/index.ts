import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { HttpError, servePost } from "../_shared/http.ts";
import { createUserClient } from "../_shared/supabase.ts";
import { musicTagPushRequestSchema } from "../_shared/sync.ts";

servePost(musicTagPushRequestSchema, async ({ musicTagList }, request) => {
  const client = createUserClient(request);
  const { data, error } = await client.rpc("push_music_tags", { items: musicTagList });

  if (error) {
    console.error("push_music_tags failed", error);
    throw new HttpError(500, "sync_push_failed");
  }

  return { affectedCount: data };
});
