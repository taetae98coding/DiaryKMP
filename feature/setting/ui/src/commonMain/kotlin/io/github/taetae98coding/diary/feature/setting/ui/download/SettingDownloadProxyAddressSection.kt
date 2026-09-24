package io.github.taetae98coding.diary.feature.setting.ui.download

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.listitem.DiarySegmentedListItemDefaults
import io.github.taetae98coding.diary.compose.core.listitem.DiaryStaticSegmentedListItem
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.previewMusicDownloadProxyAddressList
import io.github.taetae98coding.diary.feature.setting.ui.setting_download_no_address_message
import io.github.taetae98coding.diary.feature.setting.ui.setting_download_this_device_address_description
import io.github.taetae98coding.diary.feature.setting.ui.setting_download_this_device_address_label
import io.github.taetae98coding.diary.feature.setting.ui.setting_download_unavailable_message
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingDownloadProxyAddressSection(
    modifier: Modifier = Modifier,
    addressList: List<String> = emptyList(),
    isUnavailable: Boolean = false,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
    ) {
        Column {
            Text(
                text = stringResource(Res.string.setting_download_this_device_address_label),
                style = DiaryTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(Res.string.setting_download_this_device_address_description),
                color = DiaryTheme.colorScheme.onSurfaceVariant,
                style = DiaryTheme.typography.bodySmall,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(DiarySegmentedListItemDefaults.Gap)) {
            when {
                isUnavailable -> {
                    DiaryStaticSegmentedListItem {
                        Text(text = stringResource(Res.string.setting_download_unavailable_message))
                    }
                }

                addressList.isEmpty() -> {
                    DiaryStaticSegmentedListItem {
                        Text(text = stringResource(Res.string.setting_download_no_address_message))
                    }
                }

                else -> {
                    addressList.forEachIndexed { index, address ->
                        DiaryStaticSegmentedListItem(
                            index = index,
                            count = addressList.size,
                        ) {
                            Text(text = address)
                        }
                    }
                }
            }
        }
    }
}

private data class SettingDownloadProxyAddressSectionPreviewValue(
    val addressList: List<String>,
    val isUnavailable: Boolean,
)

private class SettingDownloadProxyAddressSectionPreviewParameter : PreviewParameterProvider<SettingDownloadProxyAddressSectionPreviewValue> {
    override val values: Sequence<SettingDownloadProxyAddressSectionPreviewValue> =
        sequenceOf(
            SettingDownloadProxyAddressSectionPreviewValue(addressList = previewMusicDownloadProxyAddressList(), isUnavailable = false),
            SettingDownloadProxyAddressSectionPreviewValue(addressList = emptyList(), isUnavailable = false),
            SettingDownloadProxyAddressSectionPreviewValue(addressList = emptyList(), isUnavailable = true),
        )
}

@ComponentPreview
@Composable
private fun SettingDownloadProxyAddressSectionPreview(
    @PreviewParameter(SettingDownloadProxyAddressSectionPreviewParameter::class) value: SettingDownloadProxyAddressSectionPreviewValue,
) {
    DiaryTheme {
        SettingDownloadProxyAddressSection(
            addressList = value.addressList,
            isUnavailable = value.isUnavailable,
        )
    }
}
