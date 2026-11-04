package io.github.taetae98coding.diary.core.navigation

/**
 * 알림처럼 앱 밖에서 앱의 화면을 열 때 쓰는 주소다. 주소를 만드는 쪽과 해석하는 쪽이 서로의 모듈을 모르므로 여기에 둔다.
 */
public object ScreenDeepLink {
    public const val FILE_HOME: String = "diary://file-home"

    // 시간표를 여는 주소의 형태다. 중괄호 이름은 시간표 화면 키의 속성 이름과 같아야 하고, 값은 그 속성의 저장 형태(type은 day·week, date는 YYYY-MM-DD)로 담는다.
    // 서버가 일일 메모 알림에 담아 보내므로 supabase/functions/_shared/daily-memo-notification.ts의 주소와 같아야 한다.
    public const val TIMETABLE: String = "diary://timetable?type={type}&date={date}"

    // 알림의 추가 정보에 주소를 담는 키다. iOS는 알림의 userInfo로, Android는 FCM SDK가 표시한 알림을 선택했을 때 진입 화면이 받는 값으로 전달된다.
    // 서버가 같은 키로 보내므로 supabase/functions/_shared/daily-memo-notification.ts의 키와 같아야 한다.
    public const val NOTIFICATION_DATA_KEY: String = "deepLink"
}
