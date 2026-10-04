package io.github.taetae98coding.diary.compose.web

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.swipe.SwipeToDeleteBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.web.Web
import org.jetbrains.compose.resources.stringResource

@Composable
public fun SwipeWebCard(
    onEvent: (WebListEvent) -> Unit,
    modifier: Modifier = Modifier,
    web: Web? = null,
) {
    SwipeToDeleteBox(
        deleteContentDescription = stringResource(Res.string.web_list_delete_content_description),
        onDelete = { web?.let { value -> onEvent(WebListEvent.SwipeDelete(id = value.id)) } },
        modifier = modifier,
        key = web?.id,
        gesturesEnabled = web != null,
    ) {
        WebCard(
            onClick = { web?.let { value -> onEvent(WebListEvent.ClickWeb(id = value.id)) } },
            modifier = Modifier.fillMaxWidth(),
            web = web,
        )
    }
}

@ComponentPreview
@Composable
private fun SwipeWebCardPreview() {
    DiaryTheme {
        SwipeWebCard(
            onEvent = {},
            web = previewWeb(title = "웹 제목", url = "https://developer.android.com"),
        )
    }
}
