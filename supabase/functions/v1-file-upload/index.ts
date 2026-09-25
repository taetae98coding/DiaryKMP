import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import type { SupabaseClient } from "npm:@supabase/supabase-js@2";
import { FILE_BUCKET, MAX_FILE_BYTES, MAX_FILE_NAME_LENGTH } from "../_shared/file.ts";
import { HttpError, serve } from "../_shared/http.ts";
import { createUserClient, requireUserId } from "../_shared/supabase.ts";

const UNKNOWN_MIME_TYPE = "application/octet-stream";

// 헤더에는 ASCII만 실을 수 있어 앱이 파일 이름을 퍼센트 인코딩해 보낸다.
function requireFileName(request: Request): string {
  const encoded = request.headers.get("X-File-Name") ?? "";
  let name: string;

  try {
    name = decodeURIComponent(encoded);
  } catch {
    throw new HttpError(400, "invalid_parameters", { name: ["name must be percent-encoded."] });
  }

  // file 표의 char_length 제약과 같이 UTF-16 단위가 아니라 문자 단위로 센다.
  if (name.trim() === "" || [...name].length > MAX_FILE_NAME_LENGTH) {
    throw new HttpError(400, "invalid_parameters", {
      name: [`name must not be blank and must be at most ${MAX_FILE_NAME_LENGTH} characters.`],
    });
  }

  return name;
}

function readMimeType(request: Request): string {
  return request.headers.get("Content-Type")?.split(";")[0].trim() || UNKNOWN_MIME_TYPE;
}

// 본문을 흘려받기 전에 Content-Length로 먼저 거른다. 길이를 믿을 수 없는 요청은
// 읽고 난 크기로 다시 확인하고, 버킷의 file_size_limit이 마지막 경계를 맡는다.
async function readContent(request: Request): Promise<Uint8Array> {
  const contentLength = Number(request.headers.get("Content-Length"));

  if (Number.isFinite(contentLength) && contentLength > MAX_FILE_BYTES) {
    throw new HttpError(413, "file_too_large");
  }

  const content = new Uint8Array(await request.arrayBuffer());

  if (content.byteLength > MAX_FILE_BYTES) {
    throw new HttpError(413, "file_too_large");
  }

  return content;
}

serve(async (request) => {
  const client = createUserClient(request);
  const accountId = await requireUserId(client, request);
  const name = requireFileName(request);
  const mimeType = readMimeType(request);
  const content = await readContent(request);
  const id = crypto.randomUUID();
  const path = `${accountId}/${id}`;

  const { error: uploadError } = await client.storage
    .from(FILE_BUCKET)
    .upload(path, content, { contentType: mimeType, upsert: false });

  if (uploadError) {
    console.error("file upload failed", uploadError);
    throw new HttpError(500, "file_upload_failed");
  }

  const { data, error: insertError } = await client.rpc("insert_file", {
    file_id: id,
    file_name: name,
    file_mime_type: mimeType,
    file_size: content.byteLength,
    file_path: path,
  });

  if (insertError) {
    console.error("insert_file failed", insertError);
    // 등록하지 못한 내용은 아무도 참조하지 않으므로 버킷에 남기지 않는다.
    await discardUploadedFile(client, path);
    throw new HttpError(500, "file_upload_failed");
  }

  return data;
});

// 되돌리기는 이미 실패한 요청을 정리하는 단계라 여기서 다시 실패를 던지지 않고 기록만 남긴다.
async function discardUploadedFile(
  client: SupabaseClient,
  path: string,
): Promise<void> {
  const { error } = await client.storage.from(FILE_BUCKET).remove([path]);

  if (error) {
    console.error("file discard failed", error);
  }
}
