import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import type { SupabaseClient } from "npm:@supabase/supabase-js@2";
import { FILE_BUCKET, MAX_FILE_BYTES, MAX_FILE_NAME_LENGTH, MAX_FILE_TEXT_BYTES } from "../_shared/file.ts";
import { HttpError, serve } from "../_shared/http.ts";
import { createUserClient, requireUserId } from "../_shared/supabase.ts";

const UNKNOWN_MIME_TYPE = "application/octet-stream";

interface FileUpload {
  name: string;
  title: string;
  description: string;
  mimeType: string;
  content: Uint8Array;
}

// file 표의 char_length 제약과 같이 UTF-16 단위가 아니라 문자 단위로 센다.
function requireValidName(name: string): string {
  if (name.trim() === "" || [...name].length > MAX_FILE_NAME_LENGTH) {
    throw new HttpError(400, "invalid_parameters", {
      name: [`name must not be blank and must be at most ${MAX_FILE_NAME_LENGTH} characters.`],
    });
  }

  return name;
}

function requireValidTitle(title: FormDataEntryValue | null): string {
  if (typeof title !== "string" || title.trim() === "") {
    throw new HttpError(400, "invalid_parameters", { title: ["title must not be blank."] });
  }

  return title;
}

function requireKnownLength(request: Request, maxBytes: number): void {
  const contentLength = Number(request.headers.get("Content-Length"));

  if (Number.isFinite(contentLength) && contentLength > maxBytes) {
    throw new HttpError(413, "file_too_large");
  }
}

function requireContentSize(content: Uint8Array): Uint8Array {
  if (content.byteLength > MAX_FILE_BYTES) {
    throw new HttpError(413, "file_too_large");
  }

  return content;
}

function isMultipart(request: Request): boolean {
  return request.headers.get("Content-Type")?.toLowerCase().startsWith("multipart/form-data") ?? false;
}

// 제목과 설명을 함께 보내는 앱은 파일 이름, 제목, 설명과 파일 내용을 multipart 본문 하나로 보낸다.
// 이름과 제목을 먼저 확인하고 크기를 확인해야 하지만, 본문을 받아야 이름을 알 수 있어 요청 전체 크기만 먼저 거른다.
async function readMultipartUpload(request: Request): Promise<FileUpload> {
  requireKnownLength(request, MAX_FILE_BYTES + MAX_FILE_TEXT_BYTES);

  let form: FormData;

  try {
    form = await request.formData();
  } catch {
    throw new HttpError(400, "invalid_request_body");
  }

  const name = form.get("name");
  const file = form.get("file");

  if (typeof name !== "string" || !(file instanceof File)) {
    throw new HttpError(400, "invalid_parameters", { file: ["name and file are required."] });
  }

  const description = form.get("description");

  return {
    name: requireValidName(name),
    title: requireValidTitle(form.get("title")),
    description: typeof description === "string" ? description : "",
    mimeType: file.type.split(";")[0].trim() || UNKNOWN_MIME_TYPE,
    content: requireContentSize(new Uint8Array(await file.arrayBuffer())),
  };
}

// 제목을 보내지 않는 이전 버전 앱은 헤더에 퍼센트 인코딩한 파일 이름을, 본문에 파일 내용만 보낸다.
// 본문을 흘려받기 전에 Content-Length로 먼저 거르고, 길이를 믿을 수 없는 요청은 읽고 난 크기로 다시 확인한다.
async function readLegacyUpload(request: Request): Promise<FileUpload> {
  let name: string;

  try {
    name = decodeURIComponent(request.headers.get("X-File-Name") ?? "");
  } catch {
    throw new HttpError(400, "invalid_parameters", { name: ["name must be percent-encoded."] });
  }

  requireValidName(name);
  requireKnownLength(request, MAX_FILE_BYTES);

  return {
    name,
    title: name,
    description: "",
    mimeType: request.headers.get("Content-Type")?.split(";")[0].trim() || UNKNOWN_MIME_TYPE,
    content: requireContentSize(new Uint8Array(await request.arrayBuffer())),
  };
}

serve(async (request) => {
  const client = createUserClient(request);
  const accountId = await requireUserId(client, request);
  const upload = isMultipart(request) ? await readMultipartUpload(request) : await readLegacyUpload(request);
  const id = crypto.randomUUID();
  const path = `${accountId}/${id}`;

  // 버킷의 file_size_limit이 크기의 마지막 경계를 맡는다.
  const { error: uploadError } = await client.storage
    .from(FILE_BUCKET)
    .upload(path, upload.content, { contentType: upload.mimeType, upsert: false });

  if (uploadError) {
    console.error("file upload failed", uploadError);
    throw new HttpError(500, "file_upload_failed");
  }

  const { data, error: insertError } = await client.rpc("insert_file", {
    file_id: id,
    file_name: upload.name,
    file_mime_type: upload.mimeType,
    file_size: upload.content.byteLength,
    file_path: path,
    file_title: upload.title,
    file_description: upload.description,
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
