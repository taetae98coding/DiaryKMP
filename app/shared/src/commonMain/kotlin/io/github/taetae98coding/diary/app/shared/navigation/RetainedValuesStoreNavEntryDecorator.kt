package io.github.taetae98coding.diary.app.shared.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.RetainedValuesStore
import androidx.compose.runtime.retain.RetainedValuesStoreRegistry
import androidx.compose.runtime.retain.retainRetainedValuesStoreRegistry
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator

/**
 * [NavEntry]마다 전용 [RetainedValuesStore]를 제공해 entry content 안에서
 * [androidx.compose.runtime.retain.retain] 호출이 back stack 수명에 맞게 동작하도록 한다.
 * entry가 back stack에 남아 있는 동안 retained value가 유지되고, pop 시 폐기된다.
 *
 * Navigation3의 공식 retain 지원이 릴리스되면 해당 데코레이터로 교체한다.
 * https://developer.android.com/guide/navigation/navigation-3/recipes/retain?hl=en 참고.
 */
internal class RetainedValuesStoreNavEntryDecorator<T : Any>(
    registry: RetainedValuesStoreRegistry,
) : NavEntryDecorator<T>(
        onPop = { key ->
            registry.clearChild(key)
        },
        decorate = { entry ->
            registry.LocalRetainedValuesStoreProvider(entry.contentKey) { entry.Content() }
        },
    )

@Composable
internal fun <T : Any> rememberRetainedValuesStoreNavEntryDecorator(registry: RetainedValuesStoreRegistry = retainRetainedValuesStoreRegistry()): RetainedValuesStoreNavEntryDecorator<T> =
    remember(registry) {
        RetainedValuesStoreNavEntryDecorator(registry)
    }
