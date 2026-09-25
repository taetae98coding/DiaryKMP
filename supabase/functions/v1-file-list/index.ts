import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { z } from "npm:zod@3";
import { HttpError, servePost } from "../_shared/http.ts";
import { createUserClient } from "../_shared/supabase.ts";

const MAX_PAGE_SIZE = 100;

const fileListRequestSchema = z.object({
  cursor: z
    .object({
      createdAt: z.string().datetime({ offset: true }),
      id: z.string().uuid(),
    })
    .nullable(),
  size: z.number().int().min(1).max(MAX_PAGE_SIZE),
});

servePost(fileListRequestSchema, async ({ cursor, size }, request) => {
  const client = createUserClient(request);
  const { data, error } = await client.rpc("list_files", {
    created_at_cursor: cursor?.createdAt ?? null,
    id_cursor: cursor?.id ?? null,
    page_size: size,
  });

  if (error) {
    console.error("list_files failed", error);
    throw new HttpError(500, "file_list_failed");
  }

  return { fileList: data };
});
