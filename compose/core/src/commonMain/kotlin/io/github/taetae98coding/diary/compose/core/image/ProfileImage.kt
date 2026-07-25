package io.github.taetae98coding.diary.compose.core.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import io.github.taetae98coding.diary.compose.core.icon.ProfileIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun ProfileImage(
    modifier: Modifier = Modifier,
    model: Any? = null,
    contentDescription: String? = null,
) {
    val painter = rememberAsyncImagePainter(model = model)
    val state by painter.state.collectAsState()

    Box(
        // 이미지를 불러오지 못해 기본 프로필을 그리는 동안에도 이름이 남도록 바깥 노드에 둔다.
        modifier = modifier.semantics { contentDescription?.let { description -> this.contentDescription = description } },
    ) {
        if (state is AsyncImagePainter.State.Success) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            ProfileIcon(modifier = Modifier.fillMaxSize())
        }
    }
}

@ComponentPreview
@Composable
private fun ProfileImagePreview() {
    DiaryTheme {
        ProfileImage(
            modifier = Modifier.size(40.dp),
        )
    }
}
