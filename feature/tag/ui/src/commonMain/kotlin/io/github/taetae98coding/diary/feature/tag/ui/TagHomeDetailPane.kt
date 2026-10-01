package io.github.taetae98coding.diary.feature.tag.ui

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeFilterNavKey

internal fun List<ScreenNavKey>.isTagDetailOnDetailPane(): Boolean = lastOrNull { key -> key != TagHomeFilterNavKey } is TagDetailNavKey
