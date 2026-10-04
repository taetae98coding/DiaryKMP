package io.github.taetae98coding.diary.core.database.api.sync

public enum class SyncKindLocalEntity(
    public val persistentValue: String,
) {
    MEMO("memo"),
    TAG("tag"),
    PLACE("place"),
    WEB("web"),
    CONTACT("contact"),
    MUSIC("music"),
    QR("qr"),
    MEMO_TAG("memo_tag"),
    MEMO_PLACE("memo_place"),
    MEMO_WEB("memo_web"),
    MEMO_CONTACT("memo_contact"),
    TAG_LINK("tag_link"),
    WEB_TAG("web_tag"),
    PLACE_TAG("place_tag"),
}
