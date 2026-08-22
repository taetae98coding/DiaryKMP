package io.github.taetae98coding.diary.feature.setting.ui.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.listitem.DiarySegmentedListItemDefaults
import io.github.taetae98coding.diary.compose.core.listitem.DiarySelectableSegmentedListItem
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.setting_map_default_provider_label
import io.github.taetae98coding.diary.feature.setting.ui.setting_map_google_provider_label
import io.github.taetae98coding.diary.feature.setting.ui.setting_map_naver_provider_label
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingMapDefaultProviderSection(
    defaultProvider: MapProvider,
    onSelect: (MapProvider) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
    ) {
        Text(
            text = stringResource(Res.string.setting_map_default_provider_label),
            style = DiaryTheme.typography.titleMedium,
        )

        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(DiarySegmentedListItemDefaults.Gap),
        ) {
            settingMapProviderList.forEachIndexed { index, provider ->
                DiarySelectableSegmentedListItem(
                    onClick = { onSelect(provider) },
                    selected = provider == defaultProvider,
                    index = index,
                    count = settingMapProviderList.size,
                ) {
                    Text(text = provider.label())
                }
            }
        }
    }
}

@Composable
private fun MapProvider.label(): String =
    when (this) {
        MapProvider.NAVER -> stringResource(Res.string.setting_map_naver_provider_label)
        MapProvider.GOOGLE -> stringResource(Res.string.setting_map_google_provider_label)
    }

@ComponentPreview
@Composable
private fun SettingMapDefaultProviderSectionPreview() {
    DiaryTheme {
        SettingMapDefaultProviderSection(
            defaultProvider = MapProvider.NAVER,
            onSelect = {},
        )
    }
}
