import { HttpError } from "./http.ts";

const GOOGLE_CLIENT_SECRETS = JSON.parse(Deno.env.get("GOOGLE_CLIENT_SECRETS") ?? "{}") as Record<string, string>;

const GOOGLE_TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";

export async function exchangeAuthorizationCode(
  code: string,
  clientId: string,
  redirectUri: string,
  codeVerifier?: string,
): Promise<string> {
  const clientSecret = GOOGLE_CLIENT_SECRETS[clientId];
  if (!clientSecret) {
    console.error("no client secret configured for clientId", clientId);
    throw new HttpError(401, "unknown_client");
  }

  const parameters = new URLSearchParams({
    code,
    client_id: clientId,
    client_secret: clientSecret,
    redirect_uri: redirectUri,
    grant_type: "authorization_code",
  });
  if (codeVerifier) {
    parameters.set("code_verifier", codeVerifier);
  }

  const response = await fetch(GOOGLE_TOKEN_ENDPOINT, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: parameters,
  });

  const body = await response.json().catch(() => null);
  if (!response.ok || !body?.id_token) {
    console.error("google token exchange failed", body);
    throw new HttpError(401, "google_token_exchange_failed");
  }

  return body.id_token;
}
