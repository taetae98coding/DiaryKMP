import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { z } from "npm:zod@3";

const CORS_HEADERS = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type, x-region, x-file-name",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

export class HttpError extends Error {
  constructor(readonly status: number, readonly code: string, readonly details?: unknown) {
    super(code);
  }
}

function json(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...CORS_HEADERS, "Content-Type": "application/json" },
  });
}

export function serve(handler: (request: Request) => Promise<unknown> | unknown): void {
  Deno.serve(async (req: Request) => {
    if (req.method === "OPTIONS") return new Response("ok", { headers: CORS_HEADERS });
    if (req.method !== "POST") return json({ error: "method_not_allowed" }, 405);

    try {
      return json(await handler(req));
    } catch (error) {
      if (error instanceof HttpError) {
        return json(
          error.details === undefined ? { error: error.code } : { error: error.code, details: error.details },
          error.status,
        );
      }
      console.error("unhandled error", error);
      return json({ error: "internal_error" }, 500);
    }
  });
}

export function servePost<Schema extends z.ZodTypeAny>(
  schema: Schema,
  handler: (payload: z.infer<Schema>, request: Request) => Promise<unknown> | unknown,
): void {
  serve(async (req) => {
    let raw: unknown;
    try {
      raw = await req.json();
    } catch {
      throw new HttpError(400, "invalid_request_body");
    }

    const parsed = schema.safeParse(raw);
    if (!parsed.success) {
      throw new HttpError(400, "invalid_parameters", parsed.error.flatten().fieldErrors);
    }

    return handler(parsed.data, req);
  });
}
