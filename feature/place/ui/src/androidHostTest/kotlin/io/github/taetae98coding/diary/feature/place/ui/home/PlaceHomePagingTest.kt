package io.github.taetae98coding.diary.feature.place.ui.home

import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.testing.asPagingSourceFactory
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.feature.place.ui.home.viewmode.PlaceHomeViewMode
import io.github.taetae98coding.diary.feature.place.ui.refreshableList
import io.github.taetae98coding.diary.feature.place.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Instant
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

// 기기에 저장된 장소를 한 번에 PAGE_SIZE씩 나누어 불러오는 목록을 실제 페이지 조회로 구성한다.
// 첫 조회는 앞쪽 한 페이지만 불러오고 나머지 자리는 아직 준비되지 않은 자리로 둔다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class PlaceHomePagingTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-88a3612c-c78f-4756-b62b-e86aa65f3a19 목록 모드에서 목록을 내려 보면 아직 준비되지 않은 구간의 장소가 제목 오름차순에 이어서 나타난다`() {
        val placeList = sortedByTitlePlaceList()
        composeRule.setContent {
            val placePagingItems =
                remember {
                    Pager(
                        config =
                            PagingConfig(
                                pageSize = PAGE_SIZE,
                                initialLoadSize = PAGE_SIZE,
                                prefetchDistance = 1,
                                enablePlaceholders = true,
                            ),
                        pagingSourceFactory = placeList.asPagingSourceFactory(),
                    ).flow
                }.collectAsLazyPagingItems()

            DiaryTheme {
                PlaceHomeScaffold(
                    onEvent = {},
                    state = remember { PlaceHomeScaffoldState(initialViewMode = PlaceHomeViewMode.LIST) },
                    placePagingItems = placePagingItems,
                )
            }
        }
        composeRule.waitUntil(timeoutMillis = LOAD_TIMEOUT_MILLIS) { isDisplayed(placeList.first().detail.title) }
        composeRule.onNodeWithText(placeList.last().detail.title).assertDoesNotExist()

        // 준비되지 않은 자리는 자리 표시 카드 크기로 놓였다가 장소가 채워지면 크기가 바뀌므로,
        // 사용자가 끝에 닿을 때까지 이어서 내리는 것처럼 끝으로 다시 이동한다.
        composeRule.waitUntil(timeoutMillis = LOAD_TIMEOUT_MILLIS) {
            scrollToLast(placeList = placeList)
            isDisplayed(placeList.last().detail.title)
        }

        composeRule.onNodeWithText(placeList.last().detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(placeList[placeList.lastIndex - 1].detail.title).assertIsDisplayed()
    }

    private fun scrollToLast(placeList: List<Place>) {
        composeRule.refreshableList().performScrollToIndex(placeList.lastIndex)
        composeRule.waitForIdle()
    }

    private fun isDisplayed(text: String): Boolean =
        runCatching { composeRule.onNodeWithText(text).assertIsDisplayed() }
            .isSuccess

    private companion object {
        private const val PAGE_SIZE = 20
        private const val PLACE_COUNT = PAGE_SIZE * 3
        private const val LOAD_TIMEOUT_MILLIS = 5_000L

        private fun sortedByTitlePlaceList(): List<Place> = List(PLACE_COUNT) { index -> place(title = "PagedPlaceIndex${index.toString().padStart(length = 3, padChar = '0')}") }

        private fun place(title: String): Place =
            Place(
                id = fixtureMonkey.giveMeOne<Uuid>(),
                detail =
                    fixtureMonkey
                        .giveMeKotlinBuilder<PlaceDetail>()
                        .setExp(PlaceDetail::title, title)
                        .sample(),
                isDeleted = false,
                updatedAt = fixtureMonkey.giveMeOne<Instant>(),
                createdAt = fixtureMonkey.giveMeOne<Instant>(),
            )
    }
}
