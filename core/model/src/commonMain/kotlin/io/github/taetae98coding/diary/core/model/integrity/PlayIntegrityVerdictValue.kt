package io.github.taetae98coding.diary.core.model.integrity

public sealed interface PlayIntegrityVerdictValue {
    public data class Group(
        val fieldMap: Map<String, PlayIntegrityVerdictValue>,
    ) : PlayIntegrityVerdictValue

    public data class ValueList(
        val valueList: List<PlayIntegrityVerdictValue>,
    ) : PlayIntegrityVerdictValue

    public data class Text(
        val value: String,
    ) : PlayIntegrityVerdictValue

    public data class Number(
        val value: kotlin.Number,
    ) : PlayIntegrityVerdictValue

    public data class Flag(
        val value: Boolean,
    ) : PlayIntegrityVerdictValue

    public data object Null : PlayIntegrityVerdictValue
}
