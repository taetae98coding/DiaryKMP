package io.github.taetae98coding.diary.feature.more.ui.home.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.image.ProfileImage
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.more.ui.Res
import io.github.taetae98coding.diary.feature.more.ui.home.MoreHomeScaffoldEvent
import io.github.taetae98coding.diary.feature.more.ui.more_guest_label
import io.github.taetae98coding.diary.feature.more.ui.more_profile_image_content_description
import io.github.taetae98coding.diary.feature.more.ui.more_profile_photo_picker_click_label
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MoreHomeProfileRow(
    onEvent: (MoreHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> MoreHomeAccountUiState = { MoreHomeAccountUiState.Loading },
) {
    val uiState = uiStateProvider()
    val profileImage =
        when (uiState) {
            is MoreHomeAccountUiState.Loading -> null
            is MoreHomeAccountUiState.Guest -> null
            is MoreHomeAccountUiState.User -> uiState.profileImage
        }
    val email =
        when (uiState) {
            is MoreHomeAccountUiState.Loading -> null
            is MoreHomeAccountUiState.Guest -> stringResource(Res.string.more_guest_label)
            is MoreHomeAccountUiState.User -> uiState.email
        }

    val photoPickerClickLabel = stringResource(Res.string.more_profile_photo_picker_click_label)
    val profileClickableModifier =
        if (uiState is MoreHomeAccountUiState.User) {
            Modifier.clickable(onClickLabel = photoPickerClickLabel) { onEvent(MoreHomeScaffoldEvent.ClickProfile) }
        } else {
            Modifier
        }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProfileImage(
            model = profileImage,
            modifier =
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .then(profileClickableModifier),
            contentDescription = stringResource(Res.string.more_profile_image_content_description),
        )
        email?.let { email ->
            Text(
                text = email,
                style = DiaryTheme.typography.titleMediumEmphasized,
            )
        }
    }
}

@ComponentPreview
@Composable
private fun MoreHomeProfileRowPreview() {
    DiaryTheme {
        Surface {
            MoreHomeProfileRow(
                onEvent = {},
                uiStateProvider = {
                    MoreHomeAccountUiState.User(
                        profileImage = null,
                        email = "diary@example.com",
                    )
                },
            )
        }
    }
}
