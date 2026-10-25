import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { z } from "npm:zod@3";
import { FILE_BUCKET } from "../_shared/file.ts";
import { HttpError, servePost } from "../_shared/http.ts";
import { createUserClient } from "../_shared/supabase.ts";

// 임시 주소는 세션 없이 열리므로, 앱이 받기를 시작할 만큼만 짧게 연다.
const DOWNLOAD_URL_EXPIRES_IN_SECONDS = 5 * 60;

const fileDownloadRequestSchema = z.object({
  id: z.string().uuid(),
});

servePost(fileDownloadRequestSchema, async ({ id }, request) => {
  const client = createUserClient(request);
  const { data: path, error } = await client.rpc("get_file_path", { file_id: id });

  if (error) {
    console.error("get_file_path failed", error);
    throw new HttpError(500, "file_download_failed");
  }

  if (path === null) {
    throw new HttpError(404, "file_not_found");
  }

  const { data: signed, error: signError } = await client.storage
    .from(FILE_BUCKET)
    .createSignedUrl(path, DOWNLOAD_URL_EXPIRES_IN_SECONDS);

  if (signError || !signed) {
    console.error("createSignedUrl failed", signError);
    throw new HttpError(500, "file_download_failed");
  }

  return { url: signed.signedUrl };
});
