package io.github.taetae98coding.diary.compose.core.button

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/**
 * 진행 표시로 아이콘이 바뀌어도 버튼의 이름이 사라지지 않도록 이름은 아이콘이 아니라 버튼에 둔다.
 */
internal fun Modifier.buttonContentDescription(contentDescription: String?): Modifier =
    if (contentDescription == null) {
        this
    } else {
        semantics { this.contentDescription = contentDescription }
    }
