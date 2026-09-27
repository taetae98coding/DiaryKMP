package io.github.taetae98coding.diary.core.navigation

/**
 * 알림처럼 앱 밖에서 앱의 화면을 열 때 쓰는 주소다. 주소를 만드는 쪽과 해석하는 쪽이 서로의 모듈을 모르므로 여기에 둔다.
 */
public object ScreenDeepLink {
    public const val FILE_HOME: String = "diary://file-home"

    // iOS 알림의 추가 정보에 주소를 담는 키다. 앱 델리게이트가 알림을 선택했을 때 이 키로 주소를 꺼낸다.
    public const val USER_INFO_KEY: String = "deepLink"
}
