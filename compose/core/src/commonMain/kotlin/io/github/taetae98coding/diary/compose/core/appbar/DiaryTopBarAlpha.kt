package io.github.taetae98coding.diary.compose.core.appbar

import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import io.github.taetae98coding.diary.compose.core.animation.DiaryFadeAnimationSpec

private const val OPAQUE = 1F
private const val TRANSPARENT = 0F

// iOS 상단 바는 Compose 화면 위에 겹친 네이티브 뷰라 화면 전환의 투명도 변화를 받지 못해, 이전 화면이 다 사라진 뒤에야 바뀐다.
// 화면 전환을 소유한 쪽이 지금 화면의 불투명도를 알려 주면, iOS 상단 바가 그 값을 네이티브 뷰에 적용해 화면과 함께 교차 전환된다.
public val LocalDiaryTopBarAlpha: ProvidableCompositionLocal<() -> Float> = staticCompositionLocalOf { { OPAQUE } }

public class DiaryTopBarAlphaNavEntryDecorator<T : Any> :
    NavEntryDecorator<T>(
        decorate = { entry ->
            val alpha =
                LocalNavAnimatedContentScope.current.transition.animateFloat(
                    transitionSpec = { DiaryFadeAnimationSpec },
                    label = "DiaryTopBarAlpha",
                ) { state -> if (state == EnterExitState.Visible) OPAQUE else TRANSPARENT }
            val alphaProvider = remember(alpha) { { alpha.value } }

            CompositionLocalProvider(LocalDiaryTopBarAlpha provides alphaProvider) {
                entry.Content()
            }
        },
    )

@Composable
public fun <T : Any> rememberDiaryTopBarAlphaNavEntryDecorator(): DiaryTopBarAlphaNavEntryDecorator<T> = remember { DiaryTopBarAlphaNavEntryDecorator() }
