package io.github.taetae98coding.diary.feature.tag.ui.detail.scope

import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_scope_child_label
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_scope_descendant_label
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_scope_self_label
import org.jetbrains.compose.resources.StringResource

internal fun tagDetailScopeLabel(scope: TagScope): StringResource =
    when (scope) {
        TagScope.SELF -> Res.string.tag_detail_scope_self_label
        TagScope.CHILD -> Res.string.tag_detail_scope_child_label
        TagScope.DESCENDANT -> Res.string.tag_detail_scope_descendant_label
    }
