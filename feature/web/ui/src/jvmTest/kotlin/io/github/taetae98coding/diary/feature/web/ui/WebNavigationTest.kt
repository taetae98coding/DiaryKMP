package io.github.taetae98coding.diary.feature.web.ui

import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.web.api.WebAddNavKey
import io.github.taetae98coding.diary.feature.web.api.WebHomeNavKey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull

class WebNavigationTest :
    FunSpec({
        test("TC-WEB-ADD-FEATURE-017 WebHome에서 웹 추가를 선택하면 초기 태그 없는 웹 추가로 이동한다") {
            val backStack = NavBackStack<ScreenNavKey>(WebHomeNavKey)

            backStack.navigateToWebAddFromHome()

            backStack shouldContainExactly listOf(WebHomeNavKey, WebAddNavKey())
            (backStack.last() as WebAddNavKey).initialTagId.shouldBeNull()
        }
    })
