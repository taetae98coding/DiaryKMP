package io.github.taetae98coding.diary.core.datastore.api.setting.entity

public enum class MapProviderLocalEntity(
    public val persistentValue: String,
) {
    NAVER("naver"),
    GOOGLE("google"),
    ;

    public companion object {
        public fun fromPersistentValue(persistentValue: String?): MapProviderLocalEntity? = entries.find { entry -> entry.persistentValue == persistentValue }
    }
}
