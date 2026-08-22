package io.github.taetae98coding.diary.feature.setting.ui.map

import io.github.taetae98coding.diary.core.model.map.MapProvider

internal sealed interface SettingMapScaffoldEvent {
    data object ClickNavigateUp : SettingMapScaffoldEvent

    data class SelectDefaultProvider(
        val provider: MapProvider,
    ) : SettingMapScaffoldEvent
}
