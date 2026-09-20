-- 앱이 고른 사진을 JPEG로 바꿔 올리므로 버킷도 JPEG만 받는다. 다른 형식이 올라오는 것은
-- 앱과 버킷의 약속이 어긋난 상태이므로 저장 단계에서 막는다.
update storage.buckets
set allowed_mime_types = array['image/jpeg']
where id = 'profile-image';
