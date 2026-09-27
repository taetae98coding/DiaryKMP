package io.github.taetae98coding.diary.compose.core.scene

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.viewmodel.ViewModelStoreProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreProvider
import androidx.navigation3.runtime.NavEntryDecorator

/**
 * 목록·상세 배치의 상세 placeholder는 NavEntry가 아니어서 entry decorator가 감싸지 않으므로,
 * 그대로 두면 placeholder의 저장 상태는 placeholder가 화면에서 빠질 때 사라지고 ViewModel은 앱 전체 범위에 남는다.
 * 목록 entry의 contentKey마다 저장 상태와 ViewModelStore를 따로 두고, 목록 entry가 back stack에서 빠질 때 함께 비운다.
 */
public class ListDetailPlaceholderStateHolder internal constructor(
    private val saveableStateHolder: SaveableStateHolder,
    private val viewModelStoreProvider: ViewModelStoreProvider,
) {
    public fun removeState(listContentKey: Any) {
        val key = placeholderKey(listContentKey)

        saveableStateHolder.removeState(key)
        viewModelStoreProvider.clearKey(key)
    }

    @Composable
    internal fun PlaceholderStateProvider(
        listContentKey: Any,
        content: @Composable () -> Unit,
    ) {
        val key = placeholderKey(listContentKey)

        saveableStateHolder.SaveableStateProvider(key = key) {
            val viewModelStoreOwner = rememberViewModelStoreOwner(key = key, provider = viewModelStoreProvider)

            CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner, content = content)
        }
    }
}

public val LocalListDetailPlaceholderStateHolder: ProvidableCompositionLocal<ListDetailPlaceholderStateHolder?> =
    staticCompositionLocalOf { null }

@Composable
public fun rememberListDetailPlaceholderStateHolder(): ListDetailPlaceholderStateHolder {
    val saveableStateHolder = rememberSaveableStateHolder()
    val viewModelStoreProvider = rememberViewModelStoreProvider()

    return remember(saveableStateHolder, viewModelStoreProvider) {
        ListDetailPlaceholderStateHolder(
            saveableStateHolder = saveableStateHolder,
            viewModelStoreProvider = viewModelStoreProvider,
        )
    }
}

/**
 * [listContentKey]는 placeholder를 둔 목록 entry의 contentKey와 같아야 목록 entry가 back stack에서 빠질 때 함께 비워진다.
 * [LocalListDetailPlaceholderStateHolder]가 없으면 저장 범위를 바꾸지 않고 [content]를 그대로 그린다.
 */
@Composable
public fun ListDetailPlaceholderStateProvider(
    listContentKey: Any,
    content: @Composable () -> Unit,
) {
    val holder = LocalListDetailPlaceholderStateHolder.current

    if (holder == null) {
        content()
    } else {
        holder.PlaceholderStateProvider(listContentKey = listContentKey, content = content)
    }
}

public class ListDetailPlaceholderNavEntryDecorator<T : Any>(
    holder: ListDetailPlaceholderStateHolder,
) : NavEntryDecorator<T>(
        onPop = { contentKey -> holder.removeState(listContentKey = contentKey) },
        decorate = { entry -> entry.Content() },
    )

@Composable
public fun <T : Any> rememberListDetailPlaceholderNavEntryDecorator(holder: ListDetailPlaceholderStateHolder): ListDetailPlaceholderNavEntryDecorator<T> =
    remember(holder) {
        ListDetailPlaceholderNavEntryDecorator(holder)
    }

private fun placeholderKey(listContentKey: Any): String = "ListDetailPlaceholder:$listContentKey"
