package io.github.taetae98coding.diary.feature.tag.ui.detail.scope

import androidx.compose.runtime.Immutable
import io.github.taetae98coding.diary.core.model.tag.TagScope

@Immutable
internal data class TagDetailScopeUiState(
    val scope: TagScope = TagScope.SELF,
)
