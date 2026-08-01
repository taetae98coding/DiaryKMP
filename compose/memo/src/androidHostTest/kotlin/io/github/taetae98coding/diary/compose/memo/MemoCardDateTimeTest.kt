package io.github.taetae98coding.diary.compose.memo

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Instant

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

private val ALL_DAY_SINGLE =
    MemoDateTime.AllDay(dateRange = LocalDate(year = 2026, month = 7, day = 19)..LocalDate(year = 2026, month = 7, day = 19))

private val ALL_DAY_RANGE =
    MemoDateTime.AllDay(dateRange = LocalDate(year = 2026, month = 7, day = 19)..LocalDate(year = 2026, month = 7, day = 21))

private val SAME_DAY_AFTERNOON =
    MemoDateTime.DateTime(
        start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 13, minute = 30),
        endInclusive = LocalDateTime(year = 2026, month = 7, day = 19, hour = 15, minute = 0),
    )

private val SAME_DAY_ACROSS_NOON =
    MemoDateTime.DateTime(
        start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 11, minute = 30),
        endInclusive = LocalDateTime(year = 2026, month = 7, day = 19, hour = 15, minute = 0),
    )

private val OTHER_DAY =
    MemoDateTime.DateTime(
        start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 13, minute = 30),
        endInclusive = LocalDateTime(year = 2026, month = 7, day = 20, hour = 9, minute = 0),
    )

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoCardDateTimeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "ko")
    fun `TC-MEMO-HOME-FEATURE-010 한국어 환경에서 하루 종일 기간은 날짜를 한 번만 표시한다`() {
        assertDateTimeText(ALL_DAY_SINGLE, "2026. 7. 19.")
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-010 기본 환경에서 하루 종일 기간은 날짜를 한 번만 표시한다`() {
        assertDateTimeText(ALL_DAY_SINGLE, "Jul 19, 2026")
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-MEMO-HOME-FEATURE-010 한국어 환경에서 여러 날 종일 기간은 시작과 종료 날짜를 표시한다`() {
        assertDateTimeText(ALL_DAY_RANGE, "2026. 7. 19. ~ 2026. 7. 21.")
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-010 기본 환경에서 여러 날 종일 기간은 시작과 종료 날짜를 표시한다`() {
        assertDateTimeText(ALL_DAY_RANGE, "Jul 19, 2026 ~ Jul 21, 2026")
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-MEMO-HOME-FEATURE-010 한국어 환경에서 같은 날 시각 기간은 종료의 날짜를 생략한다`() {
        assertDateTimeText(SAME_DAY_AFTERNOON, "2026. 7. 19. 오후 1:30 ~ 오후 3:00")
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-010 기본 환경에서 같은 날 시각 기간은 종료의 날짜를 생략한다`() {
        assertDateTimeText(SAME_DAY_AFTERNOON, "Jul 19, 2026 1:30 PM ~ 3:00 PM")
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-MEMO-HOME-FEATURE-010 한국어 환경에서 정오를 지나는 같은 날 기간도 오전·오후를 모두 표시한다`() {
        assertDateTimeText(SAME_DAY_ACROSS_NOON, "2026. 7. 19. 오전 11:30 ~ 오후 3:00")
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-010 기본 환경에서 정오를 지나는 같은 날 기간도 오전·오후를 모두 표시한다`() {
        assertDateTimeText(SAME_DAY_ACROSS_NOON, "Jul 19, 2026 11:30 AM ~ 3:00 PM")
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-MEMO-HOME-FEATURE-010 한국어 환경에서 다른 날 시각 기간은 종료의 날짜와 시각을 표시한다`() {
        assertDateTimeText(OTHER_DAY, "2026. 7. 19. 오후 1:30 ~ 2026. 7. 20. 오전 9:00")
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-010 기본 환경에서 다른 날 시각 기간은 종료의 날짜와 시각을 표시한다`() {
        assertDateTimeText(OTHER_DAY, "Jul 19, 2026 1:30 PM ~ Jul 20, 2026 9:00 AM")
    }

    private fun assertDateTimeText(
        dateTime: MemoDateTime,
        expected: String,
    ) {
        val memo =
            fixtureMonkey
                .giveMeKotlinBuilder<Memo>()
                .setExp(
                    Memo::detail,
                    fixtureMonkey.giveMeOne<MemoDetail>().copy(dateTime = dateTime),
                ).setExp(
                    Memo::updatedAt,
                    Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
                ).setExp(
                    Memo::createdAt,
                    Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
                ).sample()

        composeRule.setContent {
            DiaryTheme {
                MemoCard(
                    memo = memo,
                    onClick = {},
                )
            }
        }

        composeRule
            .onNodeWithTag(
                testTag = MEMO_DATE_TIME_TEST_TAG,
                useUnmergedTree = true,
            ).assertTextEquals(expected)
    }
}
