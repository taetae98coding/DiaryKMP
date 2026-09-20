package io.github.taetae98coding.diary.work.daily.memo

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.memo.DailyMemo
import io.github.taetae98coding.diary.core.model.memo.DailyMemoNotificationContent
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.util.Locale

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class DailyMemoNotificationTitleTest :
    FunSpec({
        test("기기 언어가 한국어면 알림 문구가 한국어다") {
            listOf(Locale.KOREAN, Locale.KOREA).forEach { locale ->
                dailyMemoNotificationTitle(content = loaded(count = 3), locale = locale) shouldBe "오늘 확인할 메모가 3개 있어요"
                dailyMemoNotificationTitle(content = loaded(count = 1), locale = locale) shouldBe "오늘 확인할 메모가 1개 있어요"
                dailyMemoNotificationTitle(content = loaded(count = 0), locale = locale) shouldBe "오늘 확인할 메모가 없어요"
                dailyMemoNotificationTitle(content = DailyMemoNotificationContent.Unavailable, locale = locale) shouldBe "오늘의 메모를 확인하세요"
            }
        }

        test("기기 언어가 한국어가 아니면 알림 문구가 영어다") {
            listOf(Locale.ENGLISH, Locale.JAPANESE).forEach { locale ->
                dailyMemoNotificationTitle(content = loaded(count = 3), locale = locale) shouldBe "You have 3 memos to check today"
                dailyMemoNotificationTitle(content = loaded(count = 1), locale = locale) shouldBe "You have 1 memo to check today"
                dailyMemoNotificationTitle(content = loaded(count = 0), locale = locale) shouldBe "No memos to check today"
                dailyMemoNotificationTitle(content = DailyMemoNotificationContent.Unavailable, locale = locale) shouldBe "Check today's memos"
            }
        }
    })

private fun loaded(count: Int): DailyMemoNotificationContent.Loaded = DailyMemoNotificationContent.Loaded(memoList = List(size = count) { fixtureMonkey.giveMeOne<DailyMemo>() })
