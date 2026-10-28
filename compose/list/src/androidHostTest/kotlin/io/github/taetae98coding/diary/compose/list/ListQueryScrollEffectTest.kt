package io.github.taetae98coding.diary.compose.list

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class ListQueryScrollEffectTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `정렬을 바꾸면 곧바로 맨 위로 옮긴다`() {
        val sort = mutableStateOf(ListSort.TITLE)
        val listState = setList(sort = sort, itemList = mutableStateOf(defaultItemList()))

        scrollToLast { listState.firstVisibleItemIndex }

        selectSort(sort = sort, value = ListSort.RECENTLY_UPDATED)

        listState.firstVisibleItemIndex shouldBe 0
    }

    @Test
    fun `정렬이 그대로면 목록이 바뀌어도 자리를 유지한다`() {
        val sort = mutableStateOf(ListSort.TITLE)
        val itemList = mutableStateOf(defaultItemList())
        val listState = setList(sort = sort, itemList = itemList)

        scrollToLast { listState.firstVisibleItemIndex }

        selectSort(sort = sort, value = ListSort.TITLE)
        reorder(itemList = itemList)

        listState.firstVisibleItemIndex shouldBeGreaterThan 0
    }

    @Test
    fun `필터를 바꾸면 곧바로 맨 위로 옮긴다`() {
        val filter = mutableStateOf<Boolean?>(false)
        val listState = setList(sort = mutableStateOf(ListSort.TITLE), itemList = mutableStateOf(defaultItemList()), filter = filter)

        scrollToLast { listState.firstVisibleItemIndex }

        switchFilter(filter = filter, value = true)

        listState.firstVisibleItemIndex shouldBe 0
    }

    @Test
    fun `필터가 그대로면 목록이 바뀌어도 자리를 유지한다`() {
        val itemList = mutableStateOf(defaultItemList())
        val filter = mutableStateOf<Boolean?>(true)
        val listState = setList(sort = mutableStateOf(ListSort.TITLE), itemList = itemList, filter = filter)

        scrollToLast { listState.firstVisibleItemIndex }

        switchFilter(filter = filter, value = true)
        narrow(itemList = itemList)

        listState.firstVisibleItemIndex shouldBeGreaterThan 0
    }

    @Test
    fun `불러오기 전이던 필터를 처음 불러오면 목록이 바뀌어도 자리를 유지한다`() {
        val itemList = mutableStateOf(defaultItemList())
        val filter = mutableStateOf<Boolean?>(null)
        val listState = setList(sort = mutableStateOf(ListSort.TITLE), itemList = itemList, filter = filter)

        scrollToLast { listState.firstVisibleItemIndex }

        switchFilter(filter = filter, value = true)
        narrow(itemList = itemList)

        listState.firstVisibleItemIndex shouldBeGreaterThan 0
    }

    @Test
    fun `불러오기 전이던 필터를 처음 불러온 뒤 바꾸면 곧바로 맨 위로 옮긴다`() {
        val filter = mutableStateOf<Boolean?>(null)
        val listState = setList(sort = mutableStateOf(ListSort.TITLE), itemList = mutableStateOf(defaultItemList()), filter = filter)
        switchFilter(filter = filter, value = false)

        scrollToLast { listState.firstVisibleItemIndex }

        switchFilter(filter = filter, value = true)

        listState.firstVisibleItemIndex shouldBe 0
    }

    @Test
    fun `격자에서 정렬을 바꾸면 곧바로 맨 위로 옮긴다`() {
        val sort = mutableStateOf(ListSort.TITLE)
        val gridState = setGrid(sort = sort, itemList = mutableStateOf(defaultItemList()))

        scrollToLast { gridState.firstVisibleItemIndex }

        selectSort(sort = sort, value = ListSort.RECENTLY_UPDATED)

        gridState.firstVisibleItemIndex shouldBe 0
    }

    @Test
    fun `지그재그 격자에서 정렬을 바꾸면 곧바로 맨 위로 옮긴다`() {
        val sort = mutableStateOf(ListSort.TITLE)
        val staggeredGridState = setStaggeredGrid(sort = sort, itemList = mutableStateOf(defaultItemList()))

        scrollToLast { staggeredGridState.firstVisibleItemIndex }

        selectSort(sort = sort, value = ListSort.RECENTLY_UPDATED)

        staggeredGridState.firstVisibleItemIndex shouldBe 0
    }

    private fun scrollToLast(firstVisibleItemIndexProvider: () -> Int) {
        composeRule.onNodeWithTag(LIST_TEST_TAG).performScrollToIndex(ITEM_COUNT - 1)
        composeRule.waitForIdle()

        firstVisibleItemIndexProvider() shouldBeGreaterThan 0
    }

    private fun selectSort(
        sort: MutableState<ListSort>,
        value: ListSort,
    ) {
        composeRule.runOnIdle { sort.value = value }
        composeRule.waitForIdle()
    }

    private fun reorder(itemList: MutableState<List<Int>>) {
        composeRule.runOnIdle { itemList.value = itemList.value.drop(1) + itemList.value.first() }
        composeRule.waitForIdle()
    }

    private fun switchFilter(
        filter: MutableState<Boolean?>,
        value: Boolean,
    ) {
        composeRule.runOnIdle { filter.value = value }
        composeRule.waitForIdle()
    }

    private fun narrow(itemList: MutableState<List<Int>>) {
        composeRule.runOnIdle { itemList.value = itemList.value.dropLast(1) }
        composeRule.waitForIdle()
    }

    private fun setList(
        sort: MutableState<ListSort>,
        itemList: MutableState<List<Int>>,
        filter: MutableState<Boolean?> = mutableStateOf(false),
    ): LazyListState {
        lateinit var listState: LazyListState

        composeRule.setContent {
            DiaryTheme {
                val currentSort by sort
                val currentFilter by filter
                val currentItemList by itemList

                listState = rememberLazyListState()

                ListQueryScrollEffect(
                    listState = listState,
                    sortProvider = { currentSort },
                    filterProvider = { currentFilter },
                )
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .testTag(LIST_TEST_TAG),
                    state = listState,
                ) {
                    items(
                        items = currentItemList,
                        key = { item -> item },
                    ) { item -> Item(item = item) }
                }
            }
        }
        composeRule.waitForIdle()

        return listState
    }

    private fun setGrid(
        sort: MutableState<ListSort>,
        itemList: MutableState<List<Int>>,
    ): LazyGridState {
        lateinit var gridState: LazyGridState

        composeRule.setContent {
            DiaryTheme {
                val currentSort by sort
                val currentItemList by itemList

                gridState = rememberLazyGridState()

                ListQueryScrollEffect(
                    gridState = gridState,
                    sortProvider = { currentSort },
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(COLUMN_COUNT),
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .testTag(LIST_TEST_TAG),
                    state = gridState,
                ) {
                    items(
                        items = currentItemList,
                        key = { item -> item },
                    ) { item -> Item(item = item) }
                }
            }
        }
        composeRule.waitForIdle()

        return gridState
    }

    private fun setStaggeredGrid(
        sort: MutableState<ListSort>,
        itemList: MutableState<List<Int>>,
    ): LazyStaggeredGridState {
        lateinit var staggeredGridState: LazyStaggeredGridState

        composeRule.setContent {
            DiaryTheme {
                val currentSort by sort
                val currentItemList by itemList

                staggeredGridState = rememberLazyStaggeredGridState()

                ListQueryScrollEffect(
                    staggeredGridState = staggeredGridState,
                    sortProvider = { currentSort },
                )
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(COLUMN_COUNT),
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .testTag(LIST_TEST_TAG),
                    state = staggeredGridState,
                ) {
                    items(
                        items = currentItemList,
                        key = { item -> item },
                    ) { item -> Item(item = item) }
                }
            }
        }
        composeRule.waitForIdle()

        return staggeredGridState
    }

    @Composable
    private fun Item(item: Int) {
        Text(
            text = "항목 $item",
            modifier = Modifier.height(ITEM_HEIGHT),
        )
    }

    private companion object {
        private const val LIST_TEST_TAG = "ListQueryScrollEffectList"
        private const val ITEM_COUNT = 30
        private const val COLUMN_COUNT = 2
        private val ITEM_HEIGHT = 100.dp

        private fun defaultItemList(): List<Int> = List(ITEM_COUNT) { index -> index }
    }
}
