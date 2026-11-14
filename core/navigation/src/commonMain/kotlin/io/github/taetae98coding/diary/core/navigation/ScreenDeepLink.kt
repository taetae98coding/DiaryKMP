package io.github.taetae98coding.diary.core.navigation

/**
 * 알림처럼 앱 밖에서 앱의 화면을 열 때 쓰는 주소다. 주소를 만드는 쪽과 해석하는 쪽이 서로의 모듈을 모르므로 여기에 둔다.
 */
public object ScreenDeepLink {
    public const val FILE_HOME: String = "diary://file-home"

    // 시간표를 여는 주소의 형태다. 중괄호 이름은 시간표 화면 키의 속성 이름과 같아야 하고, 값은 그 속성의 저장 형태(type은 day·week, date는 YYYY-MM-DD)로 담는다.
    // 서버가 일일 메모 알림에 담아 보내므로 supabase/functions/_shared/daily-memo-notification.ts의 주소와 같아야 한다.
    public const val TIMETABLE: String = "diary://timetable?type={type}&date={date}"

    // 항목 상세 화면을 여는 주소의 형태다. 중괄호 이름은 각 상세 화면 키의 속성 이름과 같아야 하고, id는 항목 식별자의 문자열 형태로 담는다.
    // iOS Spotlight 색인이 항목마다 이 주소를 식별자로 담아, 사용자가 고른 항목을 이 주소로 연다.
    public const val MEMO_DETAIL: String = "diary://memo-detail?id={id}"
    public const val TAG_DETAIL: String = "diary://tag-detail?id={id}"
    public const val PLACE_DETAIL: String = "diary://place-detail?id={id}"
    public const val WEB_DETAIL: String = "diary://web-detail?id={id}"
    public const val CONTACT_DETAIL: String = "diary://contact-detail?id={id}"
    public const val CHECKLIST_DETAIL: String = "diary://checklist-detail?id={id}"
    public const val QR_DETAIL: String = "diary://qr-detail?id={id}"
    public const val MUSIC_DETAIL: String = "diary://music-detail?id={id}"
    public const val DDAY_DETAIL: String = "diary://dday-detail?id={id}"
    public const val ROUTINE_DETAIL: String = "diary://routine-detail?id={id}"

    // 다른 앱의 공유로 받은 내용을 채운 추가 화면을 여는 주소의 형태다. 중괄호 이름은 각 추가 화면 키(메모는 주소 값 클래스)의 속성 이름과 같아야 하고, 값이 없으면 그 이름을 빼고 담는다.
    // 전화번호처럼 여러 개인 값은 같은 이름을 반복해 담는다. 날짜는 YYYY-MM-DD, 날짜와 시각은 YYYY-MM-DDTHH:MM 형태로 담는다.
    public const val WEB_ADD: String = "diary://web-add?initialTitle={initialTitle}&initialUrl={initialUrl}"
    public const val MEMO_ADD: String =
        "diary://memo-add?initialTitle={initialTitle}&initialDescription={initialDescription}" +
            "&initialStartDate={initialStartDate}&initialEndDate={initialEndDate}" +
            "&initialStartDateTime={initialStartDateTime}&initialEndDateTime={initialEndDateTime}"
    public const val CONTACT_ADD: String =
        "diary://contact-add?initialName={initialName}&initialDescription={initialDescription}" +
            "&initialPhoneNumberList={initialPhoneNumberList}&initialBirthday={initialBirthday}"

    // iOS 공유 확장이 공유 원문을 앱에 넘기는 주소의 형태다. 앱 진입점이 이 원문으로 판정해 위의 추가 화면 주소로 바꿔 연다.
    // kind는 아래 SHARE_KIND_* 값이고, text는 글·연락처 파일·일정 파일의 글, subject는 공유 제목이다.
    // 공유 확장은 Kotlin을 쓰지 않으므로 iosApp/DiaryShare의 주소·키와 같아야 한다.
    public const val SHARE_HOST: String = "share"
    public const val SHARE_KIND: String = "kind"
    public const val SHARE_TEXT: String = "text"
    public const val SHARE_SUBJECT: String = "subject"
    public const val SHARE_KIND_TEXT: String = "text"
    public const val SHARE_KIND_CONTACT: String = "contact"
    public const val SHARE_KIND_EVENT: String = "event"

    // 알림의 추가 정보에 주소를 담는 키다. iOS는 알림의 userInfo로, Android는 FCM SDK가 표시한 알림을 선택했을 때 진입 화면이 받는 값으로 전달된다.
    // 서버가 같은 키로 보내므로 supabase/functions/_shared/daily-memo-notification.ts의 키와 같아야 한다.
    public const val NOTIFICATION_DATA_KEY: String = "deepLink"
}
