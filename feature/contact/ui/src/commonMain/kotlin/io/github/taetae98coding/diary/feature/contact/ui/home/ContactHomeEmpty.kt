package io.github.taetae98coding.diary.feature.contact.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.ContactIcon
import io.github.taetae98coding.diary.compose.core.icon.FilterIcon
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.contact.ui.Res
import io.github.taetae98coding.diary.feature.contact.ui.contact_home_empty_description
import io.github.taetae98coding.diary.feature.contact.ui.contact_home_empty_title
import io.github.taetae98coding.diary.feature.contact.ui.contact_home_filtered_empty_description
import io.github.taetae98coding.diary.feature.contact.ui.contact_home_filtered_empty_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ContactHomeEmpty(
    modifier: Modifier = Modifier,
    isFilterAppliedProvider: () -> Boolean = { false },
) {
    DiaryCrossfade(
        targetState = isFilterAppliedProvider(),
        modifier = modifier,
    ) { isFilterApplied ->
        // 빈 상태에서도 당겨서 새로고침할 수 있도록 중첩 스크롤을 전달한다.
        val emptyBoxModifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())

        if (isFilterApplied) {
            DiaryEmptyBox(
                title = stringResource(Res.string.contact_home_filtered_empty_title),
                modifier = emptyBoxModifier,
                description = stringResource(Res.string.contact_home_filtered_empty_description),
                icon = { FilterIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
            )
        } else {
            DiaryEmptyBox(
                title = stringResource(Res.string.contact_home_empty_title),
                modifier = emptyBoxModifier,
                description = stringResource(Res.string.contact_home_empty_description),
                icon = { ContactIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
            )
        }
    }
}

@ScreenPreview
@Composable
private fun ContactHomeEmptyPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isFilterApplied: Boolean,
) {
    DiaryTheme {
        ContactHomeEmpty(
            modifier = Modifier.fillMaxSize(),
            isFilterAppliedProvider = { isFilterApplied },
        )
    }
}
