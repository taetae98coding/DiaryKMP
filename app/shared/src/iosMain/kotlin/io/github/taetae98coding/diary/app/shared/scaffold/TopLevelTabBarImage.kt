package io.github.taetae98coding.diary.app.shared.scaffold

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelNavigation
import io.github.taetae98coding.diary.compose.core.graphics.rememberTemplateUIImage
import io.github.taetae98coding.diary.compose.core.icon.Article
import io.github.taetae98coding.diary.compose.core.icon.DiaryIcons
import io.github.taetae98coding.diary.compose.core.icon.MoreHoriz
import io.github.taetae98coding.diary.compose.core.icon.Repeat
import io.github.taetae98coding.diary.compose.core.icon.Tag
import io.github.taetae98coding.diary.compose.core.icon.Today
import platform.UIKit.UIImage

@Composable
internal fun rememberTopLevelTabBarImage(topLevelNavigation: TopLevelNavigation): UIImage? = rememberTemplateUIImage(imageVector = topLevelNavigation.imageVector, size = TopLevelNavigationDefaults.DestinationIconSize)

// TopLevelNavigationIcon의 XxxIcon과 같은 심벌을 고른다.
private val TopLevelNavigation.imageVector: ImageVector
    get() =
        when (this) {
            TopLevelNavigation.Memo -> DiaryIcons.Article
            TopLevelNavigation.Tag -> DiaryIcons.Tag
            TopLevelNavigation.Calendar -> DiaryIcons.Today
            TopLevelNavigation.Routine -> DiaryIcons.Repeat
            TopLevelNavigation.More -> DiaryIcons.MoreHoriz
        }
