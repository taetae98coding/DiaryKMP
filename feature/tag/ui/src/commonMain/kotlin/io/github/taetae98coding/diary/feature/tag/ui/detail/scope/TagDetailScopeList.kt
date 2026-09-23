package io.github.taetae98coding.diary.feature.tag.ui.detail.scope

import io.github.taetae98coding.diary.core.model.tag.TagScope

internal val tagDetailScopeList: List<TagScope> =
    listOf(
        TagScope.SELF,
        TagScope.CHILD,
        TagScope.DESCENDANT,
    )
