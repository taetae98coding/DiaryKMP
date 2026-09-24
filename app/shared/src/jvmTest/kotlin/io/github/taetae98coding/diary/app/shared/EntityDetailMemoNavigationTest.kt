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
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelReselectEvent
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactHomeNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceDetailNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceHomeNavKey
import io.github.taetae98coding.diary.feature.web.api.WebDetailNavKey
import io.github.taetae98coding.diary.feature.web.api.WebHomeNavKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class EntityDetailMemoNavigationTest :
    FunSpec({
        test("MemoAdd ScreenNavKey를 저장하고 복원하면 초기 연락처, 장소, 웹 ID가 유지된다") {
            val keyList: List<ScreenNavKey> =
                listOf(
                    MemoAddNavKey(initialContactId = fixtureMonkey.giveMeOne<Uuid>()),
                    MemoAddNavKey(initialPlaceId = fixtureMonkey.giveMeOne<Uuid>()),
                    MemoAddNavKey(initialWebId = fixtureMonkey.giveMeOne<Uuid>()),
                )

            keyList.forEach { key ->
                val savedState =
                    encodeToSavedState(
                        value = key,
                        configuration = AppNavKeySavedStateConfiguration,
                    )

                withClue(key) {
                    decodeFromSavedState<ScreenNavKey>(
                        savedState = savedState,
                        configuration = AppNavKeySavedStateConfiguration,
                    ) shouldBe key
                }
            }
        }

        test("TC-CONTACT-DETAIL-MEMO-FEATURE-020 TC-PLACE-DETAIL-MEMO-FEATURE-020 TC-WEB-DETAIL-MEMO-FEATURE-020 넓은 화면에서도 상세 메모 탭에서 이어진 화면에서는 내비게이션을 숨긴다") {
            val id = fixtureMonkey.giveMeOne<Uuid>()
            val moreBackStack = listOf(TopLevelNavigation.DEFAULT.key, TopLevelNavigation.More.key)
            val detailBackStackList =
                listOf(
                    moreBackStack + ContactHomeNavKey + ContactDetailNavKey(id = id) to MemoAddNavKey(initialContactId = id),
                    moreBackStack + PlaceHomeNavKey + PlaceDetailNavKey(id = id) to MemoAddNavKey(initialPlaceId = id),
                    moreBackStack + WebHomeNavKey + WebDetailNavKey(id = id) to MemoAddNavKey(initialWebId = id),
                )
            val backStackCases =
                detailBackStackList.flatMap { (detailBackStack, memoAddKey) ->
                    listOf(
                        detailBackStack + memoAddKey,
                        detailBackStack + MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
                    )
                }

            backStackCases.forEach { keys ->
                val appState = createAppState(keys = keys)

                withClue(keys) {
                    appState.isNavigationVisible.shouldBeFalse()
                    appState.currentTopLevelNavigation shouldBe TopLevelNavigation.More
                }
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
