import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { fcmTokenSubmitRequestSchema } from "../_shared/fcm-token.ts";
import { HttpError, servePost } from "../_shared/http.ts";
import { createUserClient } from "../_shared/supabase.ts";

servePost(fcmTokenSubmitRequestSchema, async (payload, request) => {
  const client = createUserClient(request);

  if (payload.timeZone != null && payload.language != null) {
    const { error } = await client.rpc("register_fcm_token", {
      token: payload.token,
      time_zone: payload.timeZone,
      language: payload.language,
    });

    if (error) {
      console.error("register_fcm_token failed", error);
      throw new HttpError(error.code === "42501" ? 401 : 500, "fcm_token_register_failed");
    }

    return {};
  }

  const { error } = await client.rpc("unregister_fcm_token", { token: payload.token });

  if (error) {
    console.error("unregister_fcm_token failed", error);
    throw new HttpError(500, "fcm_token_unregister_failed");
  }

  return {};
});
