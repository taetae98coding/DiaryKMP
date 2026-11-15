package io.github.taetae98coding.diary.feature.web.ui.detail

import io.github.taetae98coding.diary.compose.core.share.ShareContent
import io.github.taetae98coding.diary.compose.core.share.shareTextOf
import io.github.taetae98coding.diary.core.model.web.WebDetail

internal fun webShareContentOf(
    input: WebDetail,
    saved: WebDetail,
): ShareContent.Text {
    val title = input.title.ifBlank { saved.title }
    val url = input.url.ifBlank { saved.url }

    return ShareContent.Text(text = shareTextOf(title, url), title = title.trim())
}
