package io.github.taetae98coding.diary.work.spotlight

// identifier는 항목을 여는 딥링크 주소다. 사용자가 고른 항목을 앱이 받을 때 이 값만 돌려받으므로 주소를 그대로 담는다.
internal data class SpotlightItem(
    val identifier: String,
    val title: String,
    val description: String,
    val icon: SpotlightIcon,
    val iconColor: Long,
)

internal enum class SpotlightIcon {
    MEMO,
    TAG,
    PLACE,
    WEB,
    CONTACT,
    CHECKLIST,
    QR,
    MUSIC,
}
