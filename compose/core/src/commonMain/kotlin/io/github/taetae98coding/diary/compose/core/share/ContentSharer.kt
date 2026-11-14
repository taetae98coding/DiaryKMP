package io.github.taetae98coding.diary.compose.core.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

@Stable
public interface ContentSharer {
    public val isTextSupported: Boolean

    public fun isFileSupported(type: ShareFileType): Boolean

    // 사용자가 공유 시트를 닫거나 공유를 마칠 때까지 기다린다. 웹은 사용자 조작 직후에만 공유 시트를 열 수 있으므로 누른 즉시 부른다.
    // 파일을 임시 위치에 써서 넘기는 구현은 공유할 때마다 이전에 공유한 파일을 지워, 받는 앱이 다 읽은 파일이 쌓이지 않게 한다.
    public suspend fun share(content: ShareContent): ShareResult
}

public interface ContentSharerFactory {
    @Composable
    public fun rememberContentSharer(): ContentSharer
}

public fun ContentSharer.isSupported(content: ShareContent): Boolean =
    when (content) {
        is ShareContent.Text -> isTextSupported
        is ShareContent.File -> isFileSupported(type = content.type)
    }
