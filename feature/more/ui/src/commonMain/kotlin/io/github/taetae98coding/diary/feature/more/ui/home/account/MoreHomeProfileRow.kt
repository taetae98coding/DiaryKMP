package io.github.taetae98coding.diary.feature.more.ui.home.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import io.github.taetae98coding.diary.compose.core.image.ProfileImage
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.more.ui.Res
import io.github.taetae98coding.diary.feature.more.ui.home.MoreHomeScaffoldEvent
import io.github.taetae98coding.diary.feature.more.ui.more_guest_label
import io.github.taetae98coding.diary.feature.more.ui.more_profile_image_content_description
import io.github.taetae98coding.diary.feature.more.ui.more_profile_image_view_click_label
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

    val viewClickLabel = stringResource(Res.string.more_profile_image_view_click_label)
    val profileClickableModifier =
        when {
            uiState !is MoreHomeAccountUiState.User -> Modifier

            profileImage != null -> Modifier.clickable(onClickLabel = viewClickLabel) { onEvent(MoreHomeScaffoldEvent.ClickProfileImage(profileImage)) }

            // 기본 프로필 이미지 자리를 눌러도 계정 카드의 누름으로 넘어가지 않게 누름을 받아 두기만 한다.
            else -> Modifier.pointerInput(Unit) { detectTapGestures {} }
        }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MoreHomeAccountCardDefaults.ProfileImageToLabelSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProfileImage(
            model = profileImage,
            modifier =
                Modifier
                    .size(MoreHomeAccountCardDefaults.ProfileImageSize)
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
