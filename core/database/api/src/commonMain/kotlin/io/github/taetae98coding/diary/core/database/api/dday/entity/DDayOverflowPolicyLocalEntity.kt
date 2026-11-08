package io.github.taetae98coding.diary.core.database.api.dday.entity

public enum class DDayOverflowPolicyLocalEntity(
    public val persistentValue: String,
) {
    CLAMP_TO_MONTH_END("clamp_to_month_end"),
    SKIP("skip"),
    NEXT_MONTH_FIRST_DAY("next_month_first_day"),
    ;

    public companion object {
        public fun fromPersistentValue(persistentValue: String): DDayOverflowPolicyLocalEntity? = entries.find { entry -> entry.persistentValue == persistentValue }
    }
}
