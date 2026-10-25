@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.app.shared

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.calendar.api.CalendarHomeNavKey
import io.github.taetae98coding.diary.feature.calendar.api.CalendarTimetableNavKey
import io.github.taetae98coding.diary.feature.calendar.ui.calendarEntry
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.contact.ui.contactEntry
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.memo.ui.memoEntry
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

// 캘린더 홈과 시간표에서 연 화면이 어떻게 놓이는지는 각 기능이 앱에 등록한 배치 정보가 정한다.
// 실제 배치 정보를 그대로 쓰고, 화면 본문만 이름이 보이는 대역으로 바꿔 목록과 상세를 함께 쓸 수 있는 넓은 창에서 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1280dp-h1600dp")
class CalendarOpenedScreenLayoutTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `TC-6ed1e9e1-7379-459a-9f2b-1cb9cefc8aa7 캘린더 홈 화면에서 날짜 하나로 연 시간표는 단독으로 표시된다`() {
        assertShownAlone(openedKey = CalendarTimetableNavKey(type = CalendarTimetableNavKey.Type.DAY, date = SEPTEMBER_24))
    }

    @Test
    fun `TC-6ed1e9e1-7379-459a-9f2b-1cb9cefc8aa7 캘린더 홈 화면에서 한 주로 연 시간표는 단독으로 표시된다`() {
        assertShownAlone(openedKey = CalendarTimetableNavKey(type = CalendarTimetableNavKey.Type.WEEK, date = SEPTEMBER_24))
    }

    @Test
    fun `TC-7e5262b1-33cd-4ff3-b91a-8f66c55d6c9c 캘린더 홈 화면에서 연 MemoAdd 화면은 단독으로 표시된다`() {
        assertShownAlone(
            openedKey =
                MemoAddNavKey(
                    initialDateRange = MemoAddNavKey.InitialDateRange(start = SEPTEMBER_24, endInclusive = SEPTEMBER_24),
                ),
        )
    }

    @Test
    fun `TC-0f42ef17-d670-4f8a-8a3f-d0c674d0588a 캘린더 홈 화면에서 연 ContactDetail 화면은 단독으로 표시된다`() {
        assertShownAlone(openedKey = ContactDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()))
    }

    @Test
    fun `TC-d774bdc4-ce95-4a92-994b-b802410f0435 시간표에서 연 MemoDetail 화면은 단독으로 표시된다`() {
        assertShownAlone(
            openedKey = MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
            belowKeyList = listOf(CalendarHomeNavKey, SEPTEMBER_24_TIMETABLE),
        )
    }

    @Test
    fun `TC-36703e4f-3db4-4371-bf5d-d0fa4023fe54 시간표에서 연 MemoAdd 화면은 단독으로 표시된다`() {
        assertShownAlone(
            openedKey =
                MemoAddNavKey(
                    initialDateTimeRange =
                        MemoAddNavKey.InitialDateTimeRange(
                            start = LocalDateTime(date = SEPTEMBER_24, time = LocalTime(hour = 14, minute = 0)),
                            endInclusive = LocalDateTime(date = SEPTEMBER_24, time = LocalTime(hour = 15, minute = 0)),
                        ),
                ),
            belowKeyList = listOf(CalendarHomeNavKey, SEPTEMBER_24_TIMETABLE),
        )
    }

    private fun assertShownAlone(
        openedKey: ScreenNavKey,
        belowKeyList: List<ScreenNavKey> = listOf(CalendarHomeNavKey),
    ) {
        val backStack = NavBackStack<ScreenNavKey>(*(belowKeyList + openedKey).toTypedArray())
        setAppNavDisplay(backStack = backStack)

        composeRule.onNodeWithText(contentOf(openedKey)).assertIsDisplayed()
        belowKeyList.forEach { belowKey -> composeRule.onNodeWithText(contentOf(belowKey)).assertDoesNotExist() }
        composeRule.onNodeWithText(MEMO_PLACEHOLDER_TEXT).assertDoesNotExist()
    }

    private fun setAppNavDisplay(backStack: NavBackStack<ScreenNavKey>) {
        composeRule.setContent {
            val appEntryProvider =
                remember {
                    entryProvider<ScreenNavKey> {
                        calendarEntry(backStack = backStack, homeReselectEvent = emptyFlow())
                        memoEntry(backStack = backStack, homeReselectEvent = emptyFlow())
                        contactEntry(backStack = backStack)
                    }
                }

            DiaryTheme {
                NavDisplay(
                    backStack = backStack,
                    sceneStrategies = listOf(rememberDiaryListDetailSceneStrategy()),
                    entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
                    entryProvider = { key ->
                        NavEntry(key = key, metadata = appEntryProvider(key).metadata) { Text(text = contentOf(key)) }
                    },
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun contentOf(key: ScreenNavKey): String = "${key.screenName}Content"

    private companion object {
        val SEPTEMBER_24 = LocalDate(year = 2026, month = Month.SEPTEMBER, day = 24)
        val SEPTEMBER_24_TIMETABLE = CalendarTimetableNavKey(type = CalendarTimetableNavKey.Type.DAY, date = SEPTEMBER_24)
        const val MEMO_PLACEHOLDER_TEXT = "Choose a memo"
    }
}
