package io.github.taetae98coding.diary.compose.map.provider

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.map.Res
import io.github.taetae98coding.diary.compose.map.diary_map_google_provider_label
import io.github.taetae98coding.diary.compose.map.diary_map_naver_provider_label
import io.github.taetae98coding.diary.compose.map.diary_map_provider_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DiaryMapProvider.label(): String =
    when (this) {
        DiaryMapProvider.NAVER -> stringResource(Res.string.diary_map_naver_provider_label)
        DiaryMapProvider.GOOGLE -> stringResource(Res.string.diary_map_google_provider_label)
    }

@Composable
internal fun diaryMapProviderContentDescription(): String = stringResource(Res.string.diary_map_provider_content_description)
