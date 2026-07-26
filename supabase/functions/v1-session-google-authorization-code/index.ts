import { z } from "npm:zod@3";
import { servePost } from "../_shared/http.ts";
import { exchangeAuthorizationCode } from "../_shared/google-auth.ts";
import { issueSessionFromIdToken } from "../_shared/id-token.ts";

const requestSchema = z.object({
  authorizationCode: z.string().min(1),
  clientId: z.string().min(1),
  redirectUri: z.string().min(1),
  codeVerifier: z
    .string()
    .min(43)
    .max(128)
    .regex(/^[A-Za-z0-9\-._~]+$/)
    .optional(),
});

servePost(requestSchema, async ({ authorizationCode, clientId, redirectUri, codeVerifier }) => {
  const idToken = await exchangeAuthorizationCode(authorizationCode, clientId, redirectUri, codeVerifier);

  return issueSessionFromIdToken("google", idToken);
});
