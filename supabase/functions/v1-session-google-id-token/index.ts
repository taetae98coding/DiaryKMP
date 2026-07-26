import { z } from "npm:zod@3";
import { servePost } from "../_shared/http.ts";
import { issueSessionFromIdToken } from "../_shared/id-token.ts";

const requestSchema = z.object({
  idToken: z.string().min(1),
  nonce: z.string().min(1),
});

servePost(requestSchema, ({ idToken, nonce }) => issueSessionFromIdToken("google", idToken, nonce));
