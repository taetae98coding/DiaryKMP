package io.github.taetae98coding.diary.compose.map.provider

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class DiaryMapProviderListTest :
    FunSpec({
        test("제공자는 네이버, Google 순서다") {
            diaryMapProviderList shouldContainExactly
                listOf(
                    DiaryMapProvider.NAVER,
                    DiaryMapProvider.GOOGLE,
                )
        }

        test("제공자 목록은 모든 제공자를 한 번씩만 담는다") {
            diaryMapProviderList.toSet() shouldBe DiaryMapProvider.entries.toSet()
            diaryMapProviderList.size shouldBe DiaryMapProvider.entries.size
        }
    })
