package io.github.taetae98coding.diary.feature.setting.ui.browser

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.listitem.DiarySegmentedListItemDefaults
import io.github.taetae98coding.diary.compose.core.listitem.DiarySelectableSegmentedListItem
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.browser.ChromeProfile
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.previewChromeProfileList
import io.github.taetae98coding.diary.feature.setting.ui.setting_browser_chrome_profile_none_label
import io.github.taetae98coding.diary.feature.setting.ui.setting_browser_chrome_profile_unavailable_message
import io.github.taetae98coding.diary.feature.setting.ui.setting_browser_chrome_session_import_description
import io.github.taetae98coding.diary.feature.setting.ui.setting_browser_chrome_session_import_label
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingBrowserChromeProfileSection(
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    profileList: List<ChromeProfile> = emptyList(),
    selectedProfileDirectory: String = "",
    isProfileListUnavailable: Boolean = false,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
    ) {
        Column {
            Text(
                text = stringResource(Res.string.setting_browser_chrome_session_import_label),
                style = DiaryTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(Res.string.setting_browser_chrome_session_import_description),
                color = DiaryTheme.colorScheme.onSurfaceVariant,
                style = DiaryTheme.typography.bodySmall,
            )
        }

        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(DiarySegmentedListItemDefaults.Gap),
        ) {
            val count = profileList.size + 1

            DiarySelectableSegmentedListItem(
                onClick = { onSelect("") },
                selected = selectedProfileDirectory.isEmpty(),
                index = 0,
                count = count,
            ) {
                Text(text = stringResource(Res.string.setting_browser_chrome_profile_none_label))
            }

            profileList.forEachIndexed { index, profile ->
                DiarySelectableSegmentedListItem(
                    onClick = { onSelect(profile.directory) },
                    selected = profile.directory == selectedProfileDirectory,
                    index = index + 1,
                    count = count,
                ) {
                    Text(text = profile.name)
                }
            }
        }

        if (isProfileListUnavailable) {
            Text(
                text = stringResource(Res.string.setting_browser_chrome_profile_unavailable_message),
                color = DiaryTheme.colorScheme.onSurfaceVariant,
                style = DiaryTheme.typography.bodySmall,
            )
        }
    }
}

private data class SettingBrowserChromeProfileSectionPreviewValue(
    val profileList: List<ChromeProfile>,
    val isProfileListUnavailable: Boolean,
)

private class SettingBrowserChromeProfileSectionPreviewParameter : PreviewParameterProvider<SettingBrowserChromeProfileSectionPreviewValue> {
    override val values: Sequence<SettingBrowserChromeProfileSectionPreviewValue> =
        sequenceOf(
            SettingBrowserChromeProfileSectionPreviewValue(profileList = previewChromeProfileList(), isProfileListUnavailable = false),
            SettingBrowserChromeProfileSectionPreviewValue(profileList = emptyList(), isProfileListUnavailable = false),
            SettingBrowserChromeProfileSectionPreviewValue(profileList = emptyList(), isProfileListUnavailable = true),
        )
}

@ComponentPreview
@Composable
private fun SettingBrowserChromeProfileSectionPreview(
    @PreviewParameter(SettingBrowserChromeProfileSectionPreviewParameter::class) value: SettingBrowserChromeProfileSectionPreviewValue,
) {
    DiaryTheme {
        SettingBrowserChromeProfileSection(
            onSelect = {},
            profileList = value.profileList,
            selectedProfileDirectory =
                value.profileList
                    .firstOrNull()
                    ?.directory
                    .orEmpty(),
            isProfileListUnavailable = value.isProfileListUnavailable,
        )
    }
}
