package io.github.taetae98coding.diary.work.appaction.tag

import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.work.appaction.core.invalidArgument

private const val SCOPE = "scope"

public object AppActionTagScopeValue {
    public const val SELF: String = "self"
    public const val CHILD: String = "child"
    public const val DESCENDANT: String = "descendant"
}

private val tagScopeList: List<TagScope> = listOf(TagScope.SELF, TagScope.CHILD, TagScope.DESCENDANT)

private fun TagScope.toScopeValue(): String =
    when (this) {
        TagScope.SELF -> AppActionTagScopeValue.SELF
        TagScope.CHILD -> AppActionTagScopeValue.CHILD
        TagScope.DESCENDANT -> AppActionTagScopeValue.DESCENDANT
    }

internal fun String?.toTagScopeArgument(): TagScope {
    val value = this ?: return TagScope.SELF

    return tagScopeList.firstOrNull { scope -> scope.toScopeValue() == value }
        ?: invalidArgument(message = "'$SCOPE' must be one of ${tagScopeList.joinToString { scope -> scope.toScopeValue() }}.")
}
