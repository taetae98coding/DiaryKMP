package io.github.taetae98coding.diary.feature.calendar.ui

import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.calendar.api.CalendarHomeFilterNavKey
import io.github.taetae98coding.diary.feature.calendar.api.CalendarHomeNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.navigateToTagAddFromFilter
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull

class CalendarNavigationTest :
    FunSpec({
        test("TC-5e48b2cd-da3e-49eb-b2eb-4add737877af 태그 추가를 누르면 필터를 닫고 TagAdd 화면으로 이동한다") {
            val backStack = NavBackStack<ScreenNavKey>(CalendarHomeNavKey, CalendarHomeFilterNavKey)

            backStack.navigateToTagAddFromFilter()

            backStack shouldContainExactly listOf(CalendarHomeNavKey, TagAddNavKey())
        }

        test("TC-a35e1483-802b-4acd-97a6-4f079a17b36b TagAdd 화면에서 돌아오면 필터가 다시 열리지 않고 추가한 태그를 선택으로 돌려받지 않는다") {
            val backStack = NavBackStack<ScreenNavKey>(CalendarHomeNavKey, CalendarHomeFilterNavKey)

            backStack.navigateToTagAddFromFilter()
            val tagAddKey = backStack.last() as TagAddNavKey
            backStack.removeLastOrNull()

            tagAddKey.requestKey.shouldBeNull()
            backStack shouldContainExactly listOf(CalendarHomeNavKey)
        }
    })
