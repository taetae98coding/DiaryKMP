package io.github.taetae98coding.diary.core.datastore.api.account.entity

public enum class LoginProviderLocalEntity(
    public val persistentValue: String,
) {
    GOOGLE("google"),
    APPLE("apple"),
    ;

    public companion object {
        public fun fromPersistentValue(persistentValue: String): LoginProviderLocalEntity? = entries.find { entry -> entry.persistentValue == persistentValue }
    }
}
