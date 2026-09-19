package io.github.taetae98coding.diary.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import kotlinx.serialization.PolymorphicSerializer

// navigation3의 rememberNavBackStack은 반환 타입이 NavBackStack<NavKey>로 고정되어 있어
// 화면 키만 담는 백스택을 만들 수 없으므로 같은 구현을 ScreenNavKey로 좁혀 둔다.
// navigation3가 요소 타입을 받는 오버로드를 추가하면 이 함수를 지우고 그것을 쓴다.
@Composable
internal fun rememberScreenNavBackStack(
    configuration: SavedStateConfiguration,
    vararg elements: ScreenNavKey,
): NavBackStack<ScreenNavKey> =
    rememberSerializable(
        configuration = configuration,
        serializer = NavBackStackSerializer(PolymorphicSerializer(ScreenNavKey::class)),
    ) {
        NavBackStack(*elements)
    }
