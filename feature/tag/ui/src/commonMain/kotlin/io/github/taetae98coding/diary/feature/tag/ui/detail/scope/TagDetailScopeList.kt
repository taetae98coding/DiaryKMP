package io.github.taetae98coding.diary.feature.tag.ui.detail.scope

import io.github.taetae98coding.diary.core.model.tag.TagScope

// 표시 범위가 놓이는 순서는 디자인이 정하므로 enum 선언 순서에 기대지 않고 이 목록이 소유한다.
internal val tagDetailScopeList: List<TagScope> =
    listOf(
        TagScope.SELF,
        TagScope.CHILD,
        TagScope.DESCENDANT,
    )
