@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.taetae98coding.diary.compose.core.loading

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun DiaryLoadingBox(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        ContainedLoadingIndicator(
            modifier =
                if (contentDescription == null) {
                    Modifier
                } else {
                    Modifier.semantics { this.contentDescription = contentDescription }
                },
        )
    }
}

@ComponentPreview
@Composable
private fun DiaryLoadingBoxPreview() {
    DiaryTheme {
        Surface {
            DiaryLoadingBox(modifier = Modifier.fillMaxSize())
        }
    }
}
