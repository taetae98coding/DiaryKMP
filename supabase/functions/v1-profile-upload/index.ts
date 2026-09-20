import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import type { SupabaseClient } from "npm:@supabase/supabase-js@2";
import { HttpError, serve } from "../_shared/http.ts";
import { createAdminClient, createUserClient, requireUserId } from "../_shared/supabase.ts";

const BUCKET = "profile-image";

// 앱이 고른 사진을 JPEG로 바꿔 올리므로 버킷의 allowed_mime_types와 같이 JPEG만 받는다.
const JPEG_MIME_TYPE = "image/jpeg";
const JPEG_EXTENSION = "jpg";

// 버킷의 file_size_limit과 같은 값이라 초과분은 업로드 전에 걸러낸다.
const MAX_CONTENT_BYTES = 5 * 1024 * 1024;

function requireJpeg(request: Request): void {
  const mimeType = request.headers.get("Content-Type")?.split(";")[0].trim() ?? "";

  if (mimeType !== JPEG_MIME_TYPE) {
    throw new HttpError(400, "invalid_parameters", { mimeType: [`mimeType must be ${JPEG_MIME_TYPE}.`] });
  }
}

// 본문을 흘려받기 전에 Content-Length로 먼저 거른다. 길이를 믿을 수 없는 요청은
// 읽고 난 크기로 다시 확인하고, 버킷의 file_size_limit이 마지막 경계를 맡는다.
async function readContent(request: Request): Promise<Uint8Array> {
  const contentLength = Number(request.headers.get("Content-Length"));

  if (Number.isFinite(contentLength) && contentLength > MAX_CONTENT_BYTES) {
    throw new HttpError(413, "profile_image_too_large");
  }

  const content = new Uint8Array(await request.arrayBuffer());

  if (content.byteLength === 0) {
    throw new HttpError(400, "invalid_request_body");
  }

  if (content.byteLength > MAX_CONTENT_BYTES) {
    throw new HttpError(413, "profile_image_too_large");
  }

  return content;
}

serve(async (request) => {
  const client = createUserClient(request);
  const accountId = await requireUserId(client, request);
  requireJpeg(request);
  const content = await readContent(request);
  const path = `${accountId}/${crypto.randomUUID()}.${JPEG_EXTENSION}`;

  const { error: uploadError } = await client.storage
    .from(BUCKET)
    .upload(path, content, { contentType: JPEG_MIME_TYPE, upsert: false });

  if (uploadError) {
    console.error("profile image upload failed", uploadError);
    throw new HttpError(500, "profile_image_upload_failed");
  }

  const { data: { publicUrl } } = client.storage.from(BUCKET).getPublicUrl(path);
  const admin = createAdminClient();
  const previousMetadata = await readUserMetadata(admin, accountId);

  await writeUserMetadata(admin, accountId, { ...previousMetadata, avatar_url: publicUrl });

  const { error: updateError } = await client.rpc("update_profile_image", { image_url: publicUrl });

  // 계정과 사용자 메타데이터가 다른 이미지를 가리키면 앱과 서버가 보는 프로필이 갈라지므로,
  // 계정을 갱신하지 못하면 앞서 바꾼 메타데이터와 올린 파일을 모두 되돌린다.
  if (updateError) {
    console.error("update_profile_image failed", updateError);
    await writeUserMetadata(admin, accountId, previousMetadata);
    await client.storage.from(BUCKET).remove([path]);
    throw new HttpError(500, "profile_image_update_failed");
  }

  await removeStaleFiles(client, accountId, path);

  return { profileImage: publicUrl };
});

async function readUserMetadata(
  admin: SupabaseClient,
  accountId: string,
): Promise<Record<string, unknown>> {
  const { data, error } = await admin.auth.admin.getUserById(accountId);

  if (error || !data.user) {
    console.error("getUserById failed", error);
    throw new HttpError(500, "profile_image_update_failed");
  }

  return data.user.user_metadata ?? {};
}

async function writeUserMetadata(
  admin: SupabaseClient,
  accountId: string,
  metadata: Record<string, unknown>,
): Promise<void> {
  const { error } = await admin.auth.admin.updateUserById(accountId, { user_metadata: metadata });

  if (error) {
    console.error("updateUserById failed", error);
    throw new HttpError(500, "profile_image_update_failed");
  }
}

// 갱신 뒤에는 이전 프로필 이미지를 아무도 참조하지 않는다. 정리에 실패해도
// 프로필 갱신 자체는 성공했으므로 요청을 실패로 만들지 않는다.
async function removeStaleFiles(
  client: SupabaseClient,
  accountId: string,
  currentPath: string,
): Promise<void> {
  const { data, error } = await client.storage.from(BUCKET).list(accountId);

  if (error) {
    console.error("profile image list failed", error);
    return;
  }

  const stalePaths = data
    .map((file) => `${accountId}/${file.name}`)
    .filter((filePath) => filePath !== currentPath);

  if (stalePaths.length === 0) return;

  const { error: removeError } = await client.storage.from(BUCKET).remove(stalePaths);

  if (removeError) {
    console.error("profile image cleanup failed", removeError);
  }
}
