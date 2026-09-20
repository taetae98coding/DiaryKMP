package io.github.taetae98coding.diary.work.daily.memo

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.util.Locale

class DailyMemoNotificationTitleTest :
    FunSpec({
        test("기기 언어가 한국어면 알림 문구가 한국어다") {
            dailyMemoNotificationTitle(locale = Locale.KOREAN) shouldBe "오늘의 메모를 확인하세요"
            dailyMemoNotificationTitle(locale = Locale.KOREA) shouldBe "오늘의 메모를 확인하세요"
        }

        test("기기 언어가 한국어가 아니면 알림 문구가 영어다") {
            dailyMemoNotificationTitle(locale = Locale.ENGLISH) shouldBe "Check today's memos"
            dailyMemoNotificationTitle(locale = Locale.JAPANESE) shouldBe "Check today's memos"
        }
    })
