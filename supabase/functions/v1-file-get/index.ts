import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { z } from "npm:zod@3";
import { HttpError, servePost } from "../_shared/http.ts";
import { createUserClient, requireUserId } from "../_shared/supabase.ts";

const fileGetRequestSchema = z.object({
  id: z.string().uuid(),
});

servePost(fileGetRequestSchema, async ({ id }, request) => {
  const client = createUserClient(request);
  // 사용자 세션이 아닌 JWT는 데이터베이스 함수의 권한 오류가 되기 전에 여기서 거절한다.
  await requireUserId(client, request);
  const { data, error } = await client.rpc("get_file", { file_id: id });

  if (error) {
    console.error("get_file failed", error);
    throw new HttpError(500, "file_get_failed");
  }

  if (data === null) {
    throw new HttpError(404, "file_not_found");
  }

  return data;
});
