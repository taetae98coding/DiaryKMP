package io.github.taetae98coding.diary.feature.login.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.icon.AppleIcon
import io.github.taetae98coding.diary.compose.core.icon.GoogleIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.login.ui.Res
import io.github.taetae98coding.diary.feature.login.ui.login_apple_sign_in_button_content_description
import io.github.taetae98coding.diary.feature.login.ui.login_google_sign_in_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LoginSignInButtonRow(
    onEvent: (LoginHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.componentSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppleSignInButton(onClick = { onEvent(LoginHomeScaffoldEvent.ClickAppleSignIn) })
        GoogleSignInButton(onClick = { onEvent(LoginHomeScaffoldEvent.ClickGoogleSignIn) })
    }
}

@Composable
private fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        GoogleIcon(contentDescription = stringResource(Res.string.login_google_sign_in_button_content_description))
    }
}

@Composable
private fun AppleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        AppleIcon(contentDescription = stringResource(Res.string.login_apple_sign_in_button_content_description))
    }
}

@ComponentPreview
@Composable
private fun LoginSignInButtonRowPreview() {
    DiaryTheme {
        Surface {
            LoginSignInButtonRow(onEvent = {})
        }
    }
}
