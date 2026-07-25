@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.taetae98coding.diary.compose.core.scene

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.rememberLifecycleOwner
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.get
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope

// Navigation 3 official Bottom Sheet recipe:
// https://developer.android.com/guide/navigation/navigation-3/recipes/bottomsheet
internal data class BottomSheetScene<T : Any>(
    override val key: T,
    override val previousEntries: List<NavEntry<T>>,
    override val overlaidEntries: List<NavEntry<T>>,
    private val entry: NavEntry<T>,
    private val modalBottomSheetProperties: ModalBottomSheetProperties,
    private val onBack: () -> Unit,
) : OverlayScene<T> {
    override val entries: List<NavEntry<T>> = listOf(entry)

    override val content: @Composable (() -> Unit) = {
        val lifecycleOwner = rememberLifecycleOwner()

        ModalBottomSheet(
            onDismissRequest = onBack,
            sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden, enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)),
            properties = modalBottomSheetProperties,
        ) {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                entry.Content()
            }
        }
    }
}

public class BottomSheetSceneStrategy<T : Any> : SceneStrategy<T> {
    @Suppress("ReturnCount")
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val lastEntry = entries.lastOrNull() ?: return null
        val bottomSheetProperties = lastEntry.metadata[BottomSheetKey] ?: return null

        return bottomSheetProperties.let { properties ->
            @Suppress("UNCHECKED_CAST")
            BottomSheetScene(
                key = lastEntry.contentKey as T,
                previousEntries = entries.dropLast(1),
                overlaidEntries = entries.dropLast(1),
                entry = lastEntry,
                modalBottomSheetProperties = properties,
                onBack = onBack,
            )
        }
    }

    public companion object {
        public fun bottomSheet(properties: ModalBottomSheetProperties = ModalBottomSheetProperties()): Map<String, Any> =
            metadata {
                put(BottomSheetKey, properties)
            }

        private object BottomSheetKey : NavMetadataKey<ModalBottomSheetProperties>
    }
}
