import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { HttpError, servePost } from "../_shared/http.ts";
import { createUserClient } from "../_shared/supabase.ts";
import { pullRequestSchema } from "../_shared/sync.ts";

servePost(pullRequestSchema, async ({ usn }, request) => {
  const client = createUserClient(request);
  const { data, error } = await client.rpc("pull_places", {
    usn_cursor: usn,
  });

  if (error) {
    console.error("pull_places failed", error);
    throw new HttpError(500, "sync_pull_failed");
  }

  return { placeList: data };
});
