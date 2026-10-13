import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { fcmTokenSubmitRequestSchema } from "../_shared/fcm-token.ts";
import { HttpError, servePost } from "../_shared/http.ts";
import { createPublicClient, createUserClient } from "../_shared/supabase.ts";

servePost(fcmTokenSubmitRequestSchema, async (payload, request) => {
  const isInstallationId = payload.installationId !== undefined;

  if (payload.timeZone != null && payload.language != null) {
    const { error } = isInstallationId
      ? await createUserClient(request).rpc("register_fcm_installation_id", {
        installation_id: payload.installationId,
        time_zone: payload.timeZone,
        language: payload.language,
      })
      : await createUserClient(request).rpc("register_fcm_token", {
        token: payload.token,
        time_zone: payload.timeZone,
        language: payload.language,
      });

    if (error) {
      console.error("fcm registration failed", { isInstallationId, error });
      throw new HttpError(error.code === "42501" ? 401 : 500, "fcm_token_register_failed");
    }

    return {};
  }

  // 해제는 게스트가 세션 없이 보내므로 Authorization을 요구하지 않는다.
  const { error } = isInstallationId
    ? await createPublicClient(request).rpc("unregister_fcm_installation_id", {
      installation_id: payload.installationId,
    })
    : await createPublicClient(request).rpc("unregister_fcm_token", { token: payload.token });

  if (error) {
    console.error("fcm unregistration failed", { isInstallationId, error });
    throw new HttpError(500, "fcm_token_unregister_failed");
  }

  return {};
});
