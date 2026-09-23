package io.github.taetae98coding.diary.app.shared

import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldState
import androidx.navigation3.runtime.NavBackStack
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.app.shared.navigation.AppNavKeySavedStateConfiguration
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelNavigation
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagMemoFinishedListNavKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class TagMemoNavigationTest :
    FunSpec({
        test("TagMemoFinishedList ScreenNavKey를 저장하고 복원하면 tagId가 유지된다") {
            val key: ScreenNavKey = TagMemoFinishedListNavKey(tagId = fixtureMonkey.giveMeOne<Uuid>())

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
            val key: ScreenNavKey = MemoAddNavKey(primaryTagId = fixtureMonkey.giveMeOne<Uuid>())

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
            val dayList = List(2) { fixtureMonkey.giveMeOne<Int>().mod(1_000_000) }.sorted()
            val key: ScreenNavKey =
                MemoAddNavKey(
                    initialDateRange =
                        MemoAddNavKey.InitialDateRange(
                            start = LocalDate.fromEpochDays(dayList[0]),
                            endInclusive = LocalDate.fromEpochDays(dayList[1]),
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
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
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
                    tagDetailBackStack + MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
                    tagMemoFinishedListBackStack,
                    tagMemoFinishedListBackStack + MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
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
        paneScaffoldDirectiveProvider = {
            PaneScaffoldDirective.Default.copy(maxHorizontalPartitions = 2)
        },
    )
