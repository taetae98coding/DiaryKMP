package io.github.taetae98coding.diary.feature.contact.ui.detail

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.navigation3.runtime.NavBackStack
import androidx.paging.PagingData
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.memo.MEMO_DATE_HEADER_TEST_TAG
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactHomeNavKey
import io.github.taetae98coding.diary.feature.contact.ui.navigateToContactDetail
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.todayIn
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Clock
import kotlin.uuid.Uuid
import java.util.TimeZone as JavaTimeZone

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ContactDetailScreenMemoReturnTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CONTACT-DETAIL-MEMO-FEATURE-007 화면에 돌아오면 바뀐 날짜 기준으로 오늘 날짜 그룹을 다시 표시한다`() {
        val originalTimeZone = JavaTimeZone.getDefault()
        val previousToday = Clock.System.todayIn(TimeZone.of(PREVIOUS_TIME_ZONE))
        val newToday = Clock.System.todayIn(TimeZone.of(NEW_TIME_ZONE))
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.CREATED)
        val newTodayMemoTitle = memoTitle(prefix = "NewToday")
        val previousTodayMemoTitle = memoTitle(prefix = "PreviousToday")

        try {
            JavaTimeZone.setDefault(JavaTimeZone.getTimeZone(PREVIOUS_TIME_ZONE))
            setScreenWithLifecycle(
                lifecycleOwner = lifecycleOwner,
                memoPagingData =
                    contactMemoPagingData(
                        itemList =
                            listOf(
                                MemoListItem.DateHeader(date = newToday),
                                MemoListItem.Content(memo = contactMemo(title = newTodayMemoTitle, dateTime = allDayMemoDateTime(newToday))),
                                MemoListItem.DateHeader(date = previousToday),
                                MemoListItem.Content(memo = contactMemo(title = previousTodayMemoTitle, dateTime = allDayMemoDateTime(previousToday))),
                            ),
                    ),
            )
            waitUntilMemoIsDisplayed(title = previousTodayMemoTitle)

            composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.STARTED }
            assertTodayHeader(today = previousToday, otherDay = newToday)

            // 화면을 벗어난 사이 날짜가 바뀐 것을 시간대 변경으로 재현한다.
            composeRule.runOnIdle {
                lifecycleOwner.currentState = Lifecycle.State.CREATED
                JavaTimeZone.setDefault(JavaTimeZone.getTimeZone(NEW_TIME_ZONE))
                lifecycleOwner.currentState = Lifecycle.State.STARTED
            }

            assertTodayHeader(today = newToday, otherDay = previousToday)
        } finally {
            JavaTimeZone.setDefault(originalTimeZone)
        }
    }

    @Test
    fun `TC-CONTACT-DETAIL-MEMO-FEATURE-019 ContactDetail 화면에서 메모 탭을 선택하면 연결된 메모 목록을 표시한다`() {
        val entryMemoTitle = memoTitle(prefix = "Entry")

        setScreenOnMemoTab(memoPagingData = contactMemoPagingData(itemList = listOf(MemoListItem.Content(memo = contactMemo(title = entryMemoTitle)))))

        waitUntilMemoIsDisplayed(title = entryMemoTitle)
        composeRule.onNodeWithText(entryMemoTitle).assertIsDisplayed()
    }

    @Test
    fun `TC-CONTACT-DETAIL-MEMO-FEATURE-019 ContactHome의 연락처 목록에서 선택한 연락처의 상세 화면으로 이동한다`() {
        val id = fixtureMonkey.giveMeOne<Uuid>()
        val backStack = NavBackStack<ScreenNavKey>(ContactHomeNavKey)

        backStack.navigateToContactDetail(id)

        backStack.toList() shouldBe listOf(ContactHomeNavKey, ContactDetailNavKey(id = id))
    }

    private fun setScreenWithLifecycle(
        lifecycleOwner: LifecycleOwner,
        memoPagingData: PagingData<MemoListItem>,
    ) {
        prepareContactDetailTabViewModels(memoPagingData = memoPagingData)

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                ContactDetailScreenTestHost {
                    ContactDetailScreen(
                        navigateUp = {},
                        navigateToMemoAdd = {},
                        navigateToMemoDetail = {},
                        id = FIRST_CONTACT_ID,
                        componentVisibleProvider = { ContactDetailScaffoldComponentVisible() },
                        viewModel = screenTestViewModel(MutableStateFlow(content())),
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.selectContactDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
    }

    private fun setScreenOnMemoTab(memoPagingData: PagingData<MemoListItem>) {
        composeRule.setContactDetailScreen(
            viewModel = screenTestViewModel(MutableStateFlow(content())),
            memoPagingData = memoPagingData,
        )
        composeRule.selectContactDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
    }

    // 머리글은 목록 위에 고정되며 순서가 바뀌므로 문구로 찾는다.
    private fun assertTodayHeader(
        today: LocalDate,
        otherDay: LocalDate,
    ) {
        composeRule.waitForIdle()
        composeRule.onAllNodes(hasTestTag(MEMO_DATE_HEADER_TEST_TAG) and hasText(DEFAULT_TODAY_HEADER)).assertCountEquals(1)
        composeRule.onAllNodes(hasTestTag(MEMO_DATE_HEADER_TEST_TAG) and hasText(otherDay.toEnglishDisplayText())).assertCountEquals(1)
        composeRule.onAllNodes(hasTestTag(MEMO_DATE_HEADER_TEST_TAG) and hasText(today.toEnglishDisplayText())).assertCountEquals(0)
    }

    private fun LocalDate.toEnglishDisplayText(): String = DateTimeFormatter.ofPattern(ENGLISH_DATE_PATTERN, Locale.ENGLISH).format(toJavaLocalDate())

    private fun waitUntilMemoIsDisplayed(title: String) {
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private companion object {
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L
        const val DEFAULT_TODAY_HEADER = "Today"

        // 두 시간대는 26시간 차이라 같은 순간에도 오늘이 항상 다르다.
        const val PREVIOUS_TIME_ZONE = "Pacific/Kiritimati"
        const val NEW_TIME_ZONE = "Etc/GMT+12"
        const val ENGLISH_DATE_PATTERN = "MMM d, yyyy"

        fun memoTitle(prefix: String): String = "ContactMemo$prefix${fixtureMonkey.giveMeOne<String>().filter(Char::isLetterOrDigit)}"

        fun content(): ContactDetailUiState.Content = ContactDetailUiState.Content(id = FIRST_CONTACT_ID, detail = testContactDetail())
    }
}
