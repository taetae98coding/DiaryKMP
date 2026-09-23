import type { SupabaseClient } from "npm:@supabase/supabase-js@2";

export const PROFILE_IMAGE_BUCKET = "profile-image";

// 계정이 보관한 프로필 이미지는 사용자가 반영한 이미지이거나 로그인 수단이 제공한 이미지다.
// 사용자가 반영한 이미지만 이 버킷에 올라가므로 주소가 이 버킷을 가리키는지로 둘을 구분한다.
export function isAppliedProfileImage(client: SupabaseClient, imageUrl: string | null): boolean {
  if (!imageUrl) return false;

  const { data: { publicUrl } } = client.storage.from(PROFILE_IMAGE_BUCKET).getPublicUrl("");

  return imageUrl.startsWith(publicUrl);
}
