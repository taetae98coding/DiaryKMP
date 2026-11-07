package io.github.taetae98coding.diary.app.shared

import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldState
import androidx.navigation3.runtime.NavBackStack
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import io.github.taetae98coding.diary.app.shared.navigation.AppNavKeySavedStateConfiguration
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelNavigation
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelReselectEvent
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagMemoFinishedListNavKey
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.localDateRange
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.mockk
import kotlin.uuid.Uuid

class TagMemoNavigationTest :
    FunSpec({
        test("TagMemoFinishedList ScreenNavKey를 저장하고 복원하면 tagId가 유지된다") {
            val key: ScreenNavKey = TagMemoFinishedListNavKey(tagId = Arb.kotlinUuid().next())

            val savedState =
                encodeToSavedState(
                    value = key,
                    configuration = AppNavKeySavedStateConfiguration,
                )

            decodeFromSavedState<ScreenNavKey>(
                savedState = savedState,
                configuration = AppNavKeySavedStateConfiguration,
            ) shouldBe key
        }

        test("MemoAdd ScreenNavKey를 저장하고 복원하면 최초 대표 태그 ID가 유지된다") {
            val key: ScreenNavKey = MemoAddNavKey(primaryTagId = Arb.kotlinUuid().next())

            val savedState =
                encodeToSavedState(
                    value = key,
                    configuration = AppNavKeySavedStateConfiguration,
                )

            decodeFromSavedState<ScreenNavKey>(
                savedState = savedState,
                configuration = AppNavKeySavedStateConfiguration,
            ) shouldBe key
        }

        test("MemoAdd ScreenNavKey를 저장하고 복원하면 초기 기간이 유지된다") {
            val dateRange = Arb.localDateRange().next()
            val key: ScreenNavKey =
                MemoAddNavKey(
                    initialDateRange =
                        MemoAddNavKey.InitialDateRange(
                            start = dateRange.start,
                            endInclusive = dateRange.endInclusive,
                        ),
                )

            val savedState =
                encodeToSavedState(
                    value = key,
                    configuration = AppNavKeySavedStateConfiguration,
                )

            decodeFromSavedState<ScreenNavKey>(
                savedState = savedState,
                configuration = AppNavKeySavedStateConfiguration,
            ) shouldBe key
        }

        test("넓은 화면에서도 TagDetail 메모 탭에서 이어진 독립 흐름에서는 내비게이션을 숨긴다") {
            val tagId = Arb.kotlinUuid().next()
            val tagDetailBackStack =
                listOf(
                    TopLevelNavigation.DEFAULT.key,
                    TopLevelNavigation.Tag.key,
                    TagDetailNavKey(id = tagId),
                )
            val tagMemoFinishedListBackStack = tagDetailBackStack + TagMemoFinishedListNavKey(tagId = tagId)
            val backStackCases =
                listOf(
                    tagDetailBackStack + MemoAddNavKey(primaryTagId = tagId),
                    tagDetailBackStack + MemoDetailNavKey(id = Arb.kotlinUuid().next()),
                    tagMemoFinishedListBackStack,
                    tagMemoFinishedListBackStack + MemoDetailNavKey(id = Arb.kotlinUuid().next()),
                )

            backStackCases.forEach { keys ->
                val appState = createAppState(keys = keys)

                appState.isNavigationVisible.shouldBeFalse()
                appState.currentTopLevelNavigation shouldBe TopLevelNavigation.Tag
            }
        }
    })

private fun createAppState(keys: List<ScreenNavKey>): AppState =
    AppState(
        backStack = NavBackStack(*keys.toTypedArray()),
        scaffoldState = mockk<NavigationSuiteScaffoldState>(relaxed = true),
        reselectEvent = TopLevelReselectEvent(),
        paneScaffoldDirectiveProvider = {
            PaneScaffoldDirective.Default.copy(maxHorizontalPartitions = 2)
        },
    )
