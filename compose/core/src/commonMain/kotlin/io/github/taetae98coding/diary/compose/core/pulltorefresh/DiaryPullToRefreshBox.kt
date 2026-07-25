package io.github.taetae98coding.diary.compose.core.pulltorefresh

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import io.github.taetae98coding.diary.compose.core.Res
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.pull_to_refresh_refreshing_content_description
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import org.jetbrains.compose.resources.stringResource

public const val PULL_TO_REFRESH_TEST_TAG: String = "PullToRefresh"

@Composable
public fun DiaryPullToRefreshBox(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    isRefreshingProvider: () -> Boolean = { false },
    content: @Composable BoxScope.() -> Unit,
) {
    val isRefreshing = isRefreshingProvider()
    val state = rememberPullToRefreshState()
    val refreshingContentDescription = stringResource(Res.string.pull_to_refresh_refreshing_content_description)

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.testTag(PULL_TO_REFRESH_TEST_TAG),
        state = state,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = state,
                isRefreshing = isRefreshing,
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .semantics {
                            if (isRefreshing) {
                                contentDescription = refreshingContentDescription
                            }
                        },
            )
        },
        content = content,
    )
}

@ComponentPreview
@Composable
private fun DiaryPullToRefreshBoxPreview() {
    DiaryTheme {
        DiaryPullToRefreshBox(onRefresh = {}) {
            Text(text = "당겨서 새로고침")
        }
    }
}
