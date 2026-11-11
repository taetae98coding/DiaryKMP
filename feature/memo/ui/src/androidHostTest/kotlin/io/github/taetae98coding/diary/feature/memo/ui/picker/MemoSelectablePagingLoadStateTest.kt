package io.github.taetae98coding.diary.feature.memo.ui.picker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.paging.isConfirmedEmpty
import io.github.taetae98coding.diary.feature.memo.ui.resetAndroidUiDispatcher
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class MemoSelectablePagingLoadStateTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @After
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun `선택할 수 있는 항목의 첫 조회가 실패하면 대상 없음으로 확정하지 않는다`() {
        val paging = MemoSelectablePaging<String>(scope = scope) { MutableStateFlow(Result.failure(IllegalStateException("first load failed"))) }
        lateinit var items: LazyPagingItems<String>

        composeRule.setContent { items = paging.selectablePagingData.collectAsLazyPagingItems() }
        composeRule.waitForIdle()

        items.itemCount shouldBe 0
        items.isConfirmedEmpty() shouldBe false
    }

    @Test
    fun `첫 조회가 실패한 흐름을 다시 수집해도 대상 없음으로 확정하지 않는다`() {
        val paging = MemoSelectablePaging<String>(scope = scope) { MutableStateFlow(Result.failure(IllegalStateException("first load failed"))) }
        var generation by mutableIntStateOf(0)
        lateinit var items: LazyPagingItems<String>
        composeRule.setContent { key(generation) { items = paging.selectablePagingData.collectAsLazyPagingItems() } }
        composeRule.waitForIdle()

        composeRule.runOnIdle { generation += 1 }
        composeRule.waitForIdle()

        items.itemCount shouldBe 0
        items.isConfirmedEmpty() shouldBe false
    }

    @Test
    fun `선택할 수 있는 항목이 없는 것으로 조회되면 대상 없음으로 확정한다`() {
        val paging = MemoSelectablePaging<String>(scope = scope) { MutableStateFlow(Result.success(settledPagingData(emptyList()))) }
        lateinit var items: LazyPagingItems<String>

        composeRule.setContent { items = paging.selectablePagingData.collectAsLazyPagingItems() }
        composeRule.waitForIdle()

        items.itemCount shouldBe 0
        items.isConfirmedEmpty() shouldBe true
    }
}

private fun settledPagingData(list: List<String>): PagingData<String> =
    PagingData.from(
        data = list,
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = true),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.NotLoading(endOfPaginationReached = true),
            ),
    )
