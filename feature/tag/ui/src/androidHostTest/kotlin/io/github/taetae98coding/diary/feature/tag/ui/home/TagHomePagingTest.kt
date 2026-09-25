package io.github.taetae98coding.diary.feature.tag.ui.home

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.testing.asPagingSourceFactory
import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.feature.tag.ui.fixtureText
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.coroutines.ContinuationInterceptor

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

// 기기에 저장된 태그를 한 번에 PAGE_SIZE씩 나누어 불러오는 목록을 실제 페이지 조회로 구성한다.
// 첫 조회는 앞쪽 한 페이지만 불러오고 나머지 자리는 아직 준비되지 않은 자리로 둔다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class TagHomePagingTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        val dispatcher = AndroidUiDispatcher.Main[ContinuationInterceptor] as AndroidUiDispatcher
        val type = AndroidUiDispatcher::class.java
        val lock = type.getDeclaredField("lock").apply { isAccessible = true }.get(dispatcher)

        synchronized(lock) {
            (type.getDeclaredField("toRunTrampolined").apply { isAccessible = true }.get(dispatcher) as MutableCollection<*>).clear()
            (type.getDeclaredField("toRunOnFrame").apply { isAccessible = true }.get(dispatcher) as MutableCollection<*>).clear()
            type.getDeclaredField("scheduledTrampolineDispatch").apply { isAccessible = true }.setBoolean(dispatcher, false)
            type.getDeclaredField("scheduledFrameDispatch").apply { isAccessible = true }.setBoolean(dispatcher, false)
        }
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-017 목록을 내려 보면 아직 준비되지 않은 구간의 태그가 이어서 나타난다`() {
        val tagList = sortedByTitleTagList()
        composeRule.setContent {
            val tagPagingItems =
                remember {
                    Pager(
                        config =
                            PagingConfig(
                                pageSize = PAGE_SIZE,
                                initialLoadSize = PAGE_SIZE,
                                prefetchDistance = 1,
                                enablePlaceholders = true,
                            ),
                        pagingSourceFactory = tagList.asPagingSourceFactory(),
                    ).flow
                }.collectAsLazyPagingItems()

            DiaryTheme {
                TagHomeScaffold(
                    onTagListEvent = {},
                    tagPagingItems = tagPagingItems,
                    onEvent = {},
                )
            }
        }
        composeRule.waitUntil(timeoutMillis = LOAD_TIMEOUT_MILLIS) { isDisplayed(tagList.first().detail.title) }
        composeRule.onNodeWithText(tagList.last().detail.title).assertDoesNotExist()

        // 준비되지 않은 자리는 자리 표시 카드 크기로 놓였다가 태그가 채워지면 크기가 바뀌므로,
        // 사용자가 끝에 닿을 때까지 이어서 내리는 것처럼 끝으로 다시 이동한다.
        composeRule.waitUntil(timeoutMillis = LOAD_TIMEOUT_MILLIS) {
            scrollToLast(tagList = tagList)
            isDisplayed(tagList.last().detail.title)
        }

        composeRule.onNodeWithText(tagList.last().detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(tagList[tagList.lastIndex - 1].detail.title).assertIsDisplayed()
    }

    private fun scrollToLast(tagList: List<Tag>) {
        composeRule.onNodeWithTag(TAG_HOME_LIST_TEST_TAG).performScrollToIndex(tagList.lastIndex)
        composeRule.waitForIdle()
    }

    private fun isDisplayed(text: String): Boolean =
        runCatching { composeRule.onNodeWithText(text).assertIsDisplayed() }
            .isSuccess

    private companion object {
        private const val PAGE_SIZE = 20
        private const val TAG_COUNT = PAGE_SIZE * 3
        private const val LOAD_TIMEOUT_MILLIS = 5_000L
        private const val TITLE_PREFIX_LENGTH = 12

        private fun sortedByTitleTagList(): List<Tag> {
            val titlePrefix = fixtureText(prefix = "PagedTag").take(TITLE_PREFIX_LENGTH)

            return List(TAG_COUNT) { index -> fixtureMonkey.tag(title = "${titlePrefix}Index${index.toString().padStart(length = 3, padChar = '0')}") }
        }
    }
}
