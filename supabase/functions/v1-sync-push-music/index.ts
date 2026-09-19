import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { HttpError, servePost } from "../_shared/http.ts";
import { createUserClient } from "../_shared/supabase.ts";
import { musicPushRequestSchema } from "../_shared/sync.ts";

servePost(musicPushRequestSchema, async ({ musicList }, request) => {
  const client = createUserClient(request);
  const { data, error } = await client.rpc("push_musics", { items: musicList });

  if (error) {
    console.error("push_musics failed", error);
    throw new HttpError(500, "sync_push_failed");
  }

  return { affectedCount: data };
});
