import "jsr:@supabase/functions-js/edge-runtime.d.ts";

// Apple은 https 복귀 주소로만 form_post 응답을 보내므로, 앱 대신 이 함수가 응답을 받아 로그인 시도가 state에 담아 둔
// 앱 복귀 주소로 그대로 넘긴다. id_token 검증은 앱이 v1-session-apple-id-token으로 전달한 뒤에 이루어진다.
// state 형식: "<token>.<base64url(returnUri)>"

const ANDROID_RETURN_URI_PATTERN = /^io\.github\.taetae98coding\.diary(\.dev)?:\/\/apple-sign-in$/;
const DESKTOP_RETURN_URI_PATTERN = /^http:\/\/127\.0\.0\.1:\d{1,5}\/callback$/;
const LOCAL_WEB_ORIGIN_PATTERN = /^http:\/\/(localhost|127\.0\.0\.1):\d{1,5}$/;
const ALLOWED_WEB_ORIGINS = new Set(
  (Deno.env.get("APPLE_SIGN_IN_WEB_ORIGINS") ?? "")
    .split(",")
    .map((origin) => origin.trim())
    .filter((origin) => origin.length > 0),
);
const FORWARDED_FIELDS = ["id_token", "state", "error"] as const;

type ReturnTarget = { kind: "redirect"; uri: string } | { kind: "web"; origin: string };

export function decodeReturnUri(state: string): string | null {
  const separator = state.indexOf(".");
  if (separator < 0) return null;

  const encoded = state.slice(separator + 1).replace(/-/g, "+").replace(/_/g, "/");
  try {
    const bytes = Uint8Array.from(atob(encoded), (character) => character.charCodeAt(0));
    const decoded = new TextDecoder("utf-8", { fatal: true }).decode(bytes);
    return decoded.length > 0 ? decoded : null;
  } catch {
    return null;
  }
}

export function resolveReturnTarget(returnUri: string): ReturnTarget | null {
  if (ANDROID_RETURN_URI_PATTERN.test(returnUri) || DESKTOP_RETURN_URI_PATTERN.test(returnUri)) {
    return { kind: "redirect", uri: returnUri };
  }
  if (LOCAL_WEB_ORIGIN_PATTERN.test(returnUri) || ALLOWED_WEB_ORIGINS.has(returnUri)) {
    return { kind: "web", origin: returnUri };
  }
  return null;
}

function forwardedParameters(form: FormData): URLSearchParams {
  const parameters = new URLSearchParams();
  for (const field of FORWARDED_FIELDS) {
    const value = form.get(field);
    if (typeof value === "string" && value.length > 0) {
      parameters.set(field, value);
    }
  }
  return parameters;
}

function redirectResponse(uri: string, parameters: URLSearchParams): Response {
  return new Response(null, {
    status: 303,
    headers: { Location: `${uri}?${parameters.toString()}`, "Cache-Control": "no-store" },
  });
}

// 로그인 창을 연 앱 창으로 결과를 넘긴다. 결과 값은 script 문자열에 들어가므로 </script> 삽입을 막기 위해 <를 이스케이프한다.
function webResponse(origin: string, parameters: URLSearchParams): Response {
  const message = {
    idToken: parameters.get("id_token"),
    state: parameters.get("state"),
    error: parameters.get("error"),
  };
  const script = `
    if (window.opener) {
      window.opener.postMessage(${JSON.stringify(message).replace(/</g, "\\u003c")}, ${JSON.stringify(origin)});
    }
    setTimeout(function () { window.close(); }, 100);
  `;
  const html = `<!doctype html><html><head><meta charset="utf-8"><title>Diary</title></head><body><script>${script}</script></body></html>`;

  return new Response(html, {
    status: 200,
    headers: { "Content-Type": "text/html; charset=utf-8", "Cache-Control": "no-store" },
  });
}

export async function handleAppleCallback(request: Request): Promise<Response> {
  if (request.method !== "POST") {
    return new Response("method_not_allowed", { status: 405 });
  }

  let form: FormData;
  try {
    form = await request.formData();
  } catch {
    return new Response("invalid_request_body", { status: 400 });
  }

  const state = form.get("state");
  const returnUri = typeof state === "string" ? decodeReturnUri(state) : null;
  const target = returnUri === null ? null : resolveReturnTarget(returnUri);

  if (target === null) {
    console.error("apple callback rejected: return uri not allowed");
    return new Response("invalid_state", { status: 400 });
  }

  const parameters = forwardedParameters(form);
  return target.kind === "redirect" ? redirectResponse(target.uri, parameters) : webResponse(target.origin, parameters);
}

if (import.meta.main) {
  Deno.serve(handleAppleCallback);
}
