import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { z } from "npm:zod@3";
import { MAX_FILE_NAME_LENGTH } from "../_shared/file.ts";
import { HttpError, servePost } from "../_shared/http.ts";
import { createUserClient } from "../_shared/supabase.ts";

// file 표의 char_length 제약과 같이 UTF-16 단위가 아니라 문자 단위로 센다.
const fileUpdateRequestSchema = z.object({
  id: z.string().uuid(),
  name: z
    .string()
    .refine((name) => name.trim() !== "" && [...name].length <= MAX_FILE_NAME_LENGTH, {
      message: `name must not be blank and must be at most ${MAX_FILE_NAME_LENGTH} characters.`,
    }),
  title: z.string().refine((title) => title.trim() !== "", { message: "title must not be blank." }),
  description: z.string(),
});

servePost(fileUpdateRequestSchema, async ({ id, name, title, description }, request) => {
  const client = createUserClient(request);
  const { data, error } = await client.rpc("update_file", {
    file_id: id,
    file_name: name,
    file_title: title,
    file_description: description,
  });

  if (error) {
    console.error("update_file failed", error);
    throw new HttpError(500, "file_update_failed");
  }

  if (data === null) {
    throw new HttpError(404, "file_not_found");
  }

  return data;
});
