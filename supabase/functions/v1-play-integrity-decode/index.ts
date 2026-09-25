import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { z } from "npm:zod@3";
import { fetchGoogleAccessToken, readFirebaseServiceAccount } from "../_shared/google-service-account.ts";
import { HttpError, servePost } from "../_shared/http.ts";

// Google은 서비스 계정의 Cloud 프로젝트에 Play Console에서 연결한 앱의 토큰만 풀어 준다.
// 요청의 packageName을 그대로 써도 다른 앱의 토큰은 풀리지 않는다.
const PLAY_INTEGRITY_SCOPE = "https://www.googleapis.com/auth/playintegrity";

const playIntegrityDecodeRequestSchema = z.object({
  token: z.string().min(1),
  packageName: z.string().min(1),
});

servePost(playIntegrityDecodeRequestSchema, async (payload) => {
  const accessToken = await fetchGoogleAccessToken(readFirebaseServiceAccount(), PLAY_INTEGRITY_SCOPE);

  const response = await fetch(
    `https://playintegrity.googleapis.com/v1/${encodeURIComponent(payload.packageName)}:decodeIntegrityToken`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${accessToken}`,
      },
      body: JSON.stringify({ integrity_token: payload.token }),
    },
  );

  const body = await response.json().catch(() => null);
  if (!response.ok || !body?.tokenPayloadExternal) {
    console.error("play integrity decode failed", response.status, body);
    throw new HttpError(502, "play_integrity_decode_failed");
  }

  return body.tokenPayloadExternal;
});
